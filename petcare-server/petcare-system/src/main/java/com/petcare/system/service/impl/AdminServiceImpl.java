package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.dto.AdminLoginDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.entity.Admin;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.AdminMapper;
import com.petcare.system.service.AdminService;
import com.petcare.system.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final LoginAttemptService loginAttemptService;

    /** Admin 登录尝试的 Redis key 前缀，避免与用户手机号冲突 */
    private static final String ADMIN_LOGIN_KEY_PREFIX = "admin:";

    @Override
    public LoginResultDTO login(AdminLoginDTO dto) {
        String loginKey = ADMIN_LOGIN_KEY_PREFIX + dto.getUsername();

        // 检查是否已被锁定（暴力破解防护）
        if (loginAttemptService.isLocked(loginKey)) {
            long remaining = loginAttemptService.getRemainingLockSeconds(loginKey);
            throw BusinessException.badRequest(
                    "登录失败次数过多，请" + remaining + "秒后再试");
        }

        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, dto.getUsername()));

        if (admin == null) {
            loginAttemptService.recordFailedAttempt(loginKey);
            throw BusinessException.badRequest("管理员不存在");
        }

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            loginAttemptService.recordFailedAttempt(loginKey);
            throw BusinessException.badRequest("密码错误");
        }

        // 状态检查：0=禁用，其他非1值也视为异常
        if (admin.getStatus() == null || !admin.getStatus().equals(1)) {
            throw BusinessException.forbidden("账号已禁用");
        }

        // 登录成功，清除失败记录
        loginAttemptService.clearAttempts(loginKey);

        // 检测首次登录（lastLoginTime 为 null → 使用默认密码，需要强制修改）
        boolean mustChangePassword = admin.getLastLoginTime() == null;

        // 更新登录信息
        admin.setLastLoginTime(java.time.LocalDateTime.now());
        adminMapper.updateById(admin);

        String token = jwtUtils.generateToken(admin.getId(), UserType.ADMIN.getCode());

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(admin.getId());
        userInfo.setPhone(admin.getPhone());
        userInfo.setNickname(admin.getRealName());
        userInfo.setRealName(admin.getRealName());
        userInfo.setUserType(UserType.ADMIN.getCode());

        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUser(userInfo);
        result.setMustChangePassword(mustChangePassword);

        if (mustChangePassword) {
            log.warn("管理员首次登录（使用默认密码），需强制修改: adminId={}, username={}",
                    admin.getId(), admin.getUsername());
        } else {
            log.info("管理员登录成功: adminId={}, username={}", admin.getId(), admin.getUsername());
        }
        return result;
    }

    @Override
    public UserInfoDTO getAdminInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(admin.getId());
        userInfo.setPhone(admin.getPhone());
        userInfo.setNickname(admin.getRealName());
        userInfo.setRealName(admin.getRealName());
        userInfo.setUserType(UserType.ADMIN.getCode());
        return userInfo;
    }

    @Override
    public void changePassword(Long adminId, String oldPassword, String newPassword) {
        // 校验新密码强度（至少8位）
        if (newPassword == null || newPassword.length() < 8) {
            throw BusinessException.badRequest("新密码不能少于8位");
        }

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw BusinessException.badRequest("旧密码不正确");
        }

        // 不允许新旧密码相同
        if (passwordEncoder.matches(newPassword, admin.getPassword())) {
            throw BusinessException.badRequest("新密码不能与旧密码相同");
        }

        // 更新密码
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminMapper.updateById(admin);

        log.info("管理员密码修改成功: adminId={}, username={}", adminId, admin.getUsername());
    }
}

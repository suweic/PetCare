package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.converter.UserConverter;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.PhoneLoginDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.dto.UserRegisterDTO;
import com.petcare.system.entity.User;
import com.petcare.system.enums.UserStatus;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.LoginAttemptService;
import com.petcare.system.service.UserService;
import com.petcare.system.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final VerificationCodeService verificationCodeService;
    private final LoginAttemptService loginAttemptService;

    @Override
    public LoginResultDTO login(UserLoginDTO dto) {
        // 检查是否已被锁定（暴力破解防护）
        if (loginAttemptService.isLocked(dto.getPhone())) {
            long remaining = loginAttemptService.getRemainingLockSeconds(dto.getPhone());
            throw BusinessException.badRequest(
                    "登录失败次数过多，请" + remaining + "秒后再试");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
                .eq(User::getUserType, UserType.CUSTOMER.getCode()));

        // 统一使用"手机号或密码错误"防止账号枚举攻击
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailedAttempt(dto.getPhone());
            throw BusinessException.badRequest("手机号或密码错误");
        }

        // 禁用账号也记录失败尝试，防止通过错误消息差异枚举账号状态
        if (user.getStatus() != UserStatus.ENABLED.getCode()) {
            loginAttemptService.recordFailedAttempt(dto.getPhone());
            throw BusinessException.badRequest("手机号或密码错误");
        }

        // 登录成功，清除失败记录
        loginAttemptService.clearAttempts(dto.getPhone());

        String token = jwtUtils.generateToken(user.getId(), user.getUserType());

        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUser(UserConverter.INSTANCE.toUserInfoDTO(user));

        log.info("用户登录成功: userId={}, phone={}", user.getId(), maskPhone(user.getPhone()));
        return result;
    }

    @Override
    @Transactional
    public UserInfoDTO register(UserRegisterDTO dto) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));

        if (existing != null) {
            throw BusinessException.badRequest("手机号已注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + phoneSuffix(dto.getPhone()));
        user.setUserType(UserType.CUSTOMER.getCode());
        user.setStatus(UserStatus.ENABLED.getCode());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw BusinessException.badRequest("手机号已注册，请直接登录");
        }
        log.info("用户注册成功: userId={}, phone={}", user.getId(), maskPhone(user.getPhone()));

        return UserConverter.INSTANCE.toUserInfoDTO(user);
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return UserConverter.INSTANCE.toUserInfoDTO(user);
    }

    @Override
    public void sendVerificationCode(String phone) {
        verificationCodeService.sendCode(phone);
    }

    @Override
    public LoginResultDTO loginByCode(PhoneLoginDTO dto) {
        // 校验验证码
        boolean verified = verificationCodeService.verifyAndConsumeCode(dto.getPhone(), dto.getCode());
        if (!verified) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        // 查找用户，不存在则自动注册
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
                .eq(User::getUserType, UserType.CUSTOMER.getCode()));

        if (user == null) {
            user = autoRegister(dto.getPhone());
        }

        // 检查账号状态
        if (user.getStatus() != UserStatus.ENABLED.getCode()) {
            throw BusinessException.forbidden("账号已禁用");
        }

        // 生成JWT
        String token = jwtUtils.generateToken(user.getId(), user.getUserType());

        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUser(UserConverter.INSTANCE.toUserInfoDTO(user));

        log.info("验证码登录成功: userId={}, phone={}", user.getId(), maskPhone(user.getPhone()));
        return result;
    }

    /**
     * 自动注册新用户（验证码登录时用户不存在则创建）。
     * <p>
     * 注意：自动注册的用户将获得一个随机安全密码，用户无法直接使用密码登录。
     * 用户首次设置密码可通过"忘记密码"功能（待实现），在此之前只能通过验证码登录。
     * 这是手机验证码登录场景下的标准做法，密码字段作为降级兜底机制。
     */
    private User autoRegister(String phone) {
        // 生成随机安全密码（验证码登录用户通过验证码认证，密码作为降级兜底机制）
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String randomPassword = Base64.getEncoder().encodeToString(randomBytes);

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(randomPassword));
        user.setNickname("用户" + phoneSuffix(phone));
        user.setUserType(UserType.CUSTOMER.getCode());
        user.setStatus(UserStatus.ENABLED.getCode());

        userMapper.insert(user);
        log.info("验证码登录自动注册: userId={}, phone={}", user.getId(), maskPhone(phone));
        return user;
    }

    /**
     * 从手机号中安全截取后 4 位作为默认昵称后缀。
     * 防御性处理非标准长度手机号，避免 StringIndexOutOfBoundsException。
     */
    private String phoneSuffix(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        int len = phone.length();
        return len > 4 ? phone.substring(len - 4) : phone;
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间用****替换。
     * 示例：13812345678 → 138****5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}

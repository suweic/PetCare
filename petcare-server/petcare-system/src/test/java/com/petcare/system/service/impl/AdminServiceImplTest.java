package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.dto.AdminLoginDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.entity.Admin;
import com.petcare.system.mapper.AdminMapper;
import com.petcare.system.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AdminServiceImpl adminService;

    // ===== 登录测试 =====

    @Test
    void shouldLoginSuccessfully() {
        Admin admin = createAdmin(1L, "admin", 1);
        admin.setLastLoginTime(java.time.LocalDateTime.now()); // 非首次登录
        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(passwordEncoder.matches("admin123", admin.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(1L, 3)).thenReturn("admin-token");

        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        LoginResultDTO result = adminService.login(dto);

        assertNotNull(result);
        assertEquals("admin-token", result.getToken());
        assertEquals("admin", result.getUser().getNickname());
        assertFalse(result.getMustChangePassword()); // 非首次登录
        verify(loginAttemptService).clearAttempts(anyString());
    }

    @Test
    void shouldDetectFirstLogin() {
        Admin admin = createAdmin(1L, "admin", 1);
        // lastLoginTime == null → 首次登录
        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(passwordEncoder.matches("admin123", admin.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(1L, 3)).thenReturn("admin-token");

        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        LoginResultDTO result = adminService.login(dto);

        assertNotNull(result);
        assertTrue(result.getMustChangePassword()); // 首次登录
    }

    @Test
    void shouldRejectWrongPassword() {
        Admin admin = createAdmin(1L, "admin", 1);
        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(passwordEncoder.matches("wrong", admin.getPassword())).thenReturn(false);

        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong");

        assertThrows(BusinessException.class, () -> adminService.login(dto));
        verify(loginAttemptService).recordFailedAttempt(anyString());
    }

    @Test
    void shouldRejectNonExistentAdmin() {
        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("password");

        assertThrows(BusinessException.class, () -> adminService.login(dto));
    }

    @Test
    void shouldRejectDisabledAdmin() {
        Admin admin = createAdmin(1L, "admin", 0); // disabled
        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(passwordEncoder.matches("admin123", admin.getPassword())).thenReturn(true);

        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        assertThrows(BusinessException.class, () -> adminService.login(dto));
    }

    // ===== 获取管理员信息测试 =====

    @Test
    void shouldGetAdminInfo() {
        Admin admin = createAdmin(1L, "admin", 1);
        admin.setPhone("13800138000");
        admin.setEmail("admin@petcare.com");
        when(adminMapper.selectById(1L)).thenReturn(admin);

        UserInfoDTO result = adminService.getAdminInfo(1L);

        assertNotNull(result);
        assertEquals("admin", result.getNickname());
        assertEquals("admin", result.getRealName());
    }

    @Test
    void shouldRejectGetNonExistentAdmin() {
        when(adminMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> adminService.getAdminInfo(999L));
    }

    // ===== 密码修改测试 =====

    @Test
    void shouldChangePasswordSuccessfully() {
        Admin admin = createAdmin(1L, "admin", 1);
        admin.setPassword("old-hash");
        when(adminMapper.selectById(1L)).thenReturn(admin);
        when(passwordEncoder.matches("oldpass", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("newpass123", "old-hash")).thenReturn(false);

        adminService.changePassword(1L, "oldpass", "newpass123");

        verify(adminMapper).updateById(admin);
    }

    @Test
    void shouldRejectShortNewPassword() {
        assertThrows(BusinessException.class,
                () -> adminService.changePassword(1L, "old", "1234567"));
    }

    @Test
    void shouldRejectSameOldAndNewPassword() {
        Admin admin = createAdmin(1L, "admin", 1);
        admin.setPassword("same-hash");
        when(adminMapper.selectById(1L)).thenReturn(admin);
        when(passwordEncoder.matches("samepass", "same-hash")).thenReturn(true);
        when(passwordEncoder.matches("samepass", "same-hash")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> adminService.changePassword(1L, "samepass", "samepass"));
    }

    @Test
    void shouldRejectWrongOldPassword() {
        Admin admin = createAdmin(1L, "admin", 1);
        admin.setPassword("correct-hash");
        when(adminMapper.selectById(1L)).thenReturn(admin);
        when(passwordEncoder.matches("wrong-old", "correct-hash")).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> adminService.changePassword(1L, "wrong-old", "newpass123"));
    }

    // ===== 辅助方法 =====

    private Admin createAdmin(Long id, String username, Integer status) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setUsername(username);
        admin.setPassword("hashed-password");
        admin.setRealName(username);
        admin.setStatus(status);
        admin.setPhone("13800138000");
        return admin;
    }
}

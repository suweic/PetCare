package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.dto.UserRegisterDTO;
import com.petcare.system.entity.User;
import com.petcare.system.enums.UserStatus;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.LoginAttemptService;
import com.petcare.system.service.VerificationCodeService;
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
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldLoginSuccessfully() {
        User user = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(1L, UserType.CUSTOMER.getCode())).thenReturn("token");

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        LoginResultDTO result = userService.login(dto);

        assertNotNull(result);
        assertEquals("token", result.getToken());
        assertEquals("13800138000", result.getUser().getPhone());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        // 安全加固：统一错误消息防止账号枚举攻击
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals("手机号或密码错误", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordMismatch() {
        User user = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("wrong");

        // 安全加固：统一错误消息防止账号枚举攻击
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals("手机号或密码错误", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserDisabled() {
        User user = createUser(1L, "13800138000", UserStatus.DISABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        // 安全加固：禁用账号也返回统一错误消息，防止通过错误差异枚举账号状态
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals("手机号或密码错误", exception.getMessage());
    }

    @Test
    void shouldRegisterSuccessfully() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });

        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        UserInfoDTO result = userService.register(dto);

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
        assertEquals(UserType.CUSTOMER.getCode(), result.getUserType());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyRegistered() {
        User existing = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals("手机号已注册", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    private User createUser(Long id, String phone, int status) {
        User user = new User();
        user.setId(id);
        user.setPhone(phone);
        user.setPassword("encoded");
        user.setUserType(UserType.CUSTOMER.getCode());
        user.setStatus(status);
        return user;
    }
}

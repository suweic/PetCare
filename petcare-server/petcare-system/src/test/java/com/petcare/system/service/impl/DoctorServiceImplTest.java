package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.dto.DoctorListItemDTO;
import com.petcare.system.dto.DoctorRegisterDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.User;
import com.petcare.system.enums.DoctorStatus;
import com.petcare.system.enums.UserStatus;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.DoctorAuditLogMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.DoctorScheduleMapper;
import com.petcare.system.mapper.EvaluationMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.dto.DoctorEvaluationDTO;
import com.petcare.system.dto.DoctorScheduleDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DoctorScheduleMapper scheduleMapper;

    @Mock
    private EvaluationMapper evaluationMapper;

    @Mock
    private DoctorAuditLogMapper auditLogMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void shouldLoginSuccessfully() {
        User user = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        Doctor doctor = createDoctor(1L, 1L, DoctorStatus.ENABLED.getCode());

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(doctorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(doctor);
        when(jwtUtils.generateToken(1L, UserType.DOCTOR.getCode())).thenReturn("doctor-token");

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        LoginResultDTO result = doctorService.login(dto);

        assertNotNull(result);
        assertEquals("doctor-token", result.getToken());
        assertEquals("13800138000", result.getUser().getPhone());
    }

    @Test
    void shouldThrowExceptionWhenDoctorUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(dto));
        assertEquals("医生不存在", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDoctorUserDisabled() {
        User user = createUser(1L, "13800138000", UserStatus.DISABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(dto));
        assertEquals("账号已禁用", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDoctorProfileNotFound() {
        User user = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(doctorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(dto));
        assertEquals("医生资料未完善", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDoctorPending() {
        User user = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        Doctor doctor = createDoctor(1L, 1L, DoctorStatus.PENDING.getCode());

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(doctorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(doctor);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(dto));
        assertEquals("医生账号正在审核中", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDoctorRejected() {
        User user = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        Doctor doctor = createDoctor(1L, 1L, DoctorStatus.REJECTED.getCode());

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(doctorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(doctor);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");

        BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(dto));
        assertEquals("医生账号审核未通过", exception.getMessage());
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
        when(doctorMapper.insert(any(Doctor.class))).thenReturn(1);

        DoctorRegisterDTO dto = new DoctorRegisterDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");
        dto.setRealName("张医生");
        dto.setDepartmentId(1L);

        assertDoesNotThrow(() -> doctorService.register(dto));
        verify(userMapper, times(1)).insert(any(User.class));
        verify(doctorMapper, times(1)).insert(any(Doctor.class));
    }

    @Test
    void shouldThrowExceptionWhenDoctorPhoneAlreadyRegistered() {
        User existing = createUser(1L, "13800138000", UserStatus.ENABLED.getCode());
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        DoctorRegisterDTO dto = new DoctorRegisterDTO();
        dto.setPhone("13800138000");
        dto.setPassword("password");
        dto.setRealName("张医生");
        dto.setDepartmentId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.register(dto));
        assertEquals("手机号已注册", exception.getMessage());
    }

    @Test
    void shouldReturnDoctorList() {
        DoctorListItemDTO item1 = new DoctorListItemDTO();
        item1.setId(1L);
        DoctorListItemDTO item2 = new DoctorListItemDTO();
        item2.setId(2L);

        Page<DoctorListItemDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(item1, item2));
        mockPage.setTotal(2);

        when(doctorMapper.selectDoctorPage(any(Page.class), eq(null), eq(null)))
                .thenReturn(mockPage);

        Page<DoctorListItemDTO> result = doctorService.getDoctorList(null, null, 1, 10);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals(1L, result.getRecords().get(0).getId());
    }

    private User createUser(Long id, String phone, int status) {
        User user = new User();
        user.setId(id);
        user.setPhone(phone);
        user.setPassword("encoded");
        user.setUserType(UserType.DOCTOR.getCode());
        user.setStatus(status);
        return user;
    }

    private Doctor createDoctor(Long id, Long userId, int status) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setUserId(userId);
        doctor.setStatus(status);
        return doctor;
    }
}

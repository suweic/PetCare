package com.petcare.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.system.dto.AdminLoginDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.User;
import com.petcare.system.enums.DoctorStatus;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final ConsultationMapper consultationMapper;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResultDTO>> login(@Valid @RequestBody AdminLoginDTO dto) {
        LoginResultDTO result = adminService.login(dto);
        return ResponseEntity.ok(Result.success("登录成功", result));
    }

    @GetMapping("/me")
    public ResponseEntity<Result<UserInfoDTO>> getAdminInfo(Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        UserInfoDTO result = adminService.getAdminInfo(adminId);
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Result<Map<String, Object>>> getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserType, UserType.CUSTOMER.getCode()));
        long totalDoctors = doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getStatus, DoctorStatus.ENABLED.getCode()));
        long totalConsultations = consultationMapper.selectCount(null);
        long todayConsultations = consultationMapper.selectCount(
                new LambdaQueryWrapper<Consultation>()
                        .ge(Consultation::getCreateTime, todayStart));
        long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserType, UserType.CUSTOMER.getCode())
                        .ge(User::getCreateTime, todayStart));
        long pendingDoctors = doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getStatus, DoctorStatus.PENDING.getCode()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalDoctors", totalDoctors);
        stats.put("totalConsultations", totalConsultations);
        stats.put("todayConsultations", todayConsultations);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("pendingDoctors", pendingDoctors);
        // TODO: 从 order 表统计实际营收（已支付订单的 actual_amount 总和）
        stats.put("revenue", 0);

        return ResponseEntity.ok(Result.success(stats));
    }
}

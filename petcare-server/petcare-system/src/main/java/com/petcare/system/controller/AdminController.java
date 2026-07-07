package com.petcare.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.security.service.TokenBlacklistService;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.dto.AdminLoginDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Department;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.User;
import com.petcare.system.enums.DoctorStatus;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final ConsultationMapper consultationMapper;
    private final DepartmentMapper departmentMapper;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResultDTO>> login(@Valid @RequestBody AdminLoginDTO dto) {
        LoginResultDTO result = adminService.login(dto);
        return ResponseEntity.ok(Result.success("登录成功", result));
    }

    /**
     * 管理员退出登录 — 将当前 Token 加入黑名单。
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token != null) {
            try {
                String jti = jwtUtils.getTokenId(token);
                long expiration = jwtUtils.getTokenExpirationMillis(token);
                tokenBlacklistService.blacklist(jti, expiration);
                log.info("管理员 Token 已撤销: jti={}", jti);
            } catch (Exception e) {
                log.warn("管理员 Token 黑名单处理失败（可能已过期）: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(Result.success("已退出登录", null));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
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

    @GetMapping("/department/list")
    public ResponseEntity<Result<List<Department>>> getDepartmentList() {
        List<Department> list = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSort));
        return ResponseEntity.ok(Result.success(list));
    }

    /**
     * 修改管理员密码。
     * <p>
     * 用于首次登录强制修改密码和主动修改密码。
     * 新密码不能少于8位，且不能与旧密码相同。
     * </p>
     */
    @PostMapping("/change-password")
    public ResponseEntity<Result<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest req) {
        Long adminId = (Long) authentication.getPrincipal();
        adminService.changePassword(adminId, req.getOldPassword(), req.getNewPassword());
        return ResponseEntity.ok(Result.success("密码修改成功，请使用新密码重新登录", null));
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, message = "新密码不能少于8位")
        private String newPassword;
    }
}

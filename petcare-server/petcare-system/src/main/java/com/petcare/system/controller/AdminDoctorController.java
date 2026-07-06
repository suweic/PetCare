package com.petcare.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.dto.DoctorAuditDTO;
import com.petcare.system.dto.DoctorPendingDTO;
import com.petcare.system.entity.Department;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.User;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/doctor")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    @GetMapping("/pending")
    public ResponseEntity<Result<Page<DoctorPendingDTO>>> getPendingList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DoctorPendingDTO> result = doctorService.getPendingList(page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    @PostMapping("/{id}/audit")
    public ResponseEntity<Result<Void>> audit(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DoctorAuditDTO dto) {
        Long auditorId = (Long) authentication.getPrincipal();
        doctorService.audit(id, dto, auditorId);
        return ResponseEntity.ok(Result.success("审核完成", null));
    }

    @GetMapping("/list")
    public ResponseEntity<Result<Page<Map<String, Object>>>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Doctor::getStatus, status);
        }
        if (deptId != null) {
            wrapper.eq(Doctor::getDepartmentId, deptId);
        }
        wrapper.orderByDesc(Doctor::getCreateTime);
        Page<Doctor> doctorPage = doctorMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量获取关联的 user 和 department
        List<Long> userIds = doctorPage.getRecords().stream()
                .map(Doctor::getUserId).distinct().collect(Collectors.toList());
        List<Long> deptIds = doctorPage.getRecords().stream()
                .map(Doctor::getDepartmentId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Department> deptMap = deptIds.isEmpty() ? Collections.emptyMap()
                : departmentMapper.selectBatchIds(deptIds).stream()
                        .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a));

        // 按 keyword 过滤（联表条件在 Java 层处理）
        List<Map<String, Object>> records = doctorPage.getRecords().stream()
                .filter(d -> {
                    if (!StringUtils.hasText(keyword)) return true;
                    User u = userMap.get(d.getUserId());
                    String rn = u != null ? u.getRealName() : "";
                    return (rn != null && rn.contains(keyword))
                            || (d.getSpecialty() != null && d.getSpecialty().contains(keyword))
                            || (d.getHospital() != null && d.getHospital().contains(keyword))
                            || (d.getTitle() != null && d.getTitle().contains(keyword));
                })
                .map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", d.getId());
                    m.put("userId", d.getUserId());
                    User u = userMap.get(d.getUserId());
                    m.put("realName", u != null ? u.getRealName() : "");
                    m.put("phone", u != null ? u.getPhone() : "");
                    Department dept = deptMap.get(d.getDepartmentId());
                    m.put("departmentName", dept != null ? dept.getName() : "");
                    m.put("departmentId", d.getDepartmentId());
                    m.put("title", d.getTitle());
                    m.put("specialty", d.getSpecialty());
                    m.put("education", d.getEducation());
                    m.put("experience", d.getExperience());
                    m.put("hospital", d.getHospital());
                    m.put("rating", d.getRating());
                    m.put("consultationCount", d.getConsultationCount());
                    m.put("status", d.getStatus());
                    m.put("createTime", d.getCreateTime() != null ? d.getCreateTime().toString() : "");
                    return m;
                })
                .collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(page, size);
        result.setTotal(doctorPage.getTotal());
        result.setRecords(records);
        result.setPages(doctorPage.getPages());
        result.setCurrent(doctorPage.getCurrent());
        return ResponseEntity.ok(Result.success(result));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Void>> toggleStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            return ResponseEntity.ok(Result.error(404, "医生不存在"));
        }
        doctor.setStatus(status);
        doctorMapper.updateById(doctor);
        return ResponseEntity.ok(Result.success(status == 1 ? "已启用" : "已禁用", null));
    }
}

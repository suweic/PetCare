package com.petcare.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.User;
import com.petcare.system.entity.Doctor;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.mapper.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/consultation")
@RequiredArgsConstructor
public class AdminConsultationController {

    private final ConsultationMapper consultationMapper;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;

    @GetMapping("/list")
    public ResponseEntity<Result<Page<Map<String, Object>>>> getList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Consultation::getStatus, status);
        }
        if (type != null) {
            wrapper.eq(Consultation::getType, type);
        }
        wrapper.orderByDesc(Consultation::getCreateTime);
        Page<Consultation> result = consultationMapper.selectPage(new Page<>(page, size), wrapper);

        // 组装带用户/医生信息的列表
        Page<Map<String, Object>> enriched = new Page<>(page, size, result.getTotal());
        enriched.setRecords(result.getRecords().stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("userId", c.getUserId());
            map.put("doctorId", c.getDoctorId());
            map.put("petId", c.getPetId());
            map.put("departmentId", c.getDepartmentId());
            map.put("type", c.getType());
            map.put("status", c.getStatus());
            map.put("createTime", c.getCreateTime());
            map.put("updateTime", c.getUpdateTime());
            if (c.getUserId() != null) {
                User user = userMapper.selectById(c.getUserId());
                map.put("userName", user != null ? user.getNickname() : "未知");
                map.put("userPhone", user != null ? user.getPhone() : "");
            }
            if (c.getDoctorId() != null) {
                Doctor doctor = doctorMapper.selectById(c.getDoctorId());
                if (doctor != null && doctor.getUserId() != null) {
                    User doctorUser = userMapper.selectById(doctor.getUserId());
                    map.put("doctorName", doctorUser != null ? doctorUser.getRealName() : "未知");
                } else {
                    map.put("doctorName", "未知");
                }
            }
            return map;
        }).toList());
        return ResponseEntity.ok(Result.success(enriched));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Consultation>> getDetail(@PathVariable Long id) {
        Consultation consultation = consultationMapper.selectById(id);
        if (consultation == null) {
            return ResponseEntity.ok(Result.error(404, "问诊记录不存在"));
        }
        return ResponseEntity.ok(Result.success(consultation));
    }
}

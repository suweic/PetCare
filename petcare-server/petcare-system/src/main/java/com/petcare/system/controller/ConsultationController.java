package com.petcare.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.entity.Consultation;
import com.petcare.system.enums.UserType;
import com.petcare.system.service.ConsultationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问诊控制器。
 * <p>
 * 宠物主可创建、查看、取消自己的问诊；医生可查看分配给自己的问诊。
 * 通过 {@link ConsultationService} 层统一管理业务逻辑和权限校验。
 * </p>
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    /** 创建问诊（仅普通用户） */
    @PostMapping("/create")
    public ResponseEntity<Result<Consultation>> create(
            Authentication authentication,
            @Valid @RequestBody CreateRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        Consultation c = consultationService.create(
                userId, req.getPetId(), req.getDepartmentId(),
                req.getType(), req.getChiefComplaint(), req.getSymptoms());
        return ResponseEntity.ok(Result.success("创建成功", c));
    }

    /**
     * 问诊列表。
     * <p>
     * 根据 token 中的用户类型自动选择列表来源：
     * 普通用户返回自己创建的，医生返回分配给自己的。
     * </p>
     */
    @GetMapping("/list")
    public ResponseEntity<Result<Page<Consultation>>> list(
            Authentication authentication,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();

        // 从 SecurityContext 中获取角色以判断用户类型
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        Page<Consultation> result;
        if (isDoctor) {
            result = consultationService.listByDoctor(userId, status, page, size);
        } else {
            result = consultationService.listByUser(userId, status, page, size);
        }
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 问诊详情。
     * <p>
     * 宠物主和接诊医生均可查看详情，通过 Service 层进行双重授权校验。
     * </p>
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Consultation>> detail(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        Consultation c = consultationService.getDetail(id, userId);
        return ResponseEntity.ok(Result.success(c));
    }

    /** 取消问诊（仅宠物主本人可取消） */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Result<Void>> cancel(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        consultationService.cancel(id, userId);
        return ResponseEntity.ok(Result.success("已取消", null));
    }

    @Data
    public static class CreateRequest {
        @NotNull(message = "宠物ID不能为空")
        private Long petId;
        private Long departmentId;
        @NotNull(message = "问诊类型不能为空")
        private Integer type;
        private String chiefComplaint;
        private String symptoms;
    }
}

package com.petcare.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.dto.DoctorAuditDTO;
import com.petcare.system.dto.DoctorPendingDTO;
import com.petcare.system.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/doctor")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorService doctorService;

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
}

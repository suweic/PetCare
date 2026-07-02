package com.petcare.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.dto.DoctorDetailDTO;
import com.petcare.system.dto.DoctorEvaluationDTO;
import com.petcare.system.dto.DoctorListItemDTO;
import com.petcare.system.dto.DoctorRegisterDTO;
import com.petcare.system.dto.DoctorScheduleDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.PrescriptionCreateDTO;
import com.petcare.system.dto.PrescriptionDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.service.DoctorService;
import com.petcare.system.service.PrescriptionService;
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
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final PrescriptionService prescriptionService;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResultDTO>> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginResultDTO result = doctorService.login(dto);
        return ResponseEntity.ok(Result.success(result));
    }

    @PostMapping("/register")
    public ResponseEntity<Result<String>> register(@Valid @RequestBody DoctorRegisterDTO dto) {
        doctorService.register(dto);
        return ResponseEntity.ok(Result.success("注册成功，等待审核"));
    }

    @GetMapping("/list")
    public ResponseEntity<Result<Page<DoctorListItemDTO>>> getDoctorList(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DoctorListItemDTO> result = doctorService.getDoctorList(deptId, keyword, page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<DoctorDetailDTO>> getDoctorDetail(@PathVariable Long id) {
        DoctorDetailDTO result = doctorService.getDoctorDetail(id);
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<Result<Page<DoctorScheduleDTO>>> getSchedules(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DoctorScheduleDTO> result = doctorService.getDoctorSchedules(id, page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/{id}/evaluations")
    public ResponseEntity<Result<Page<DoctorEvaluationDTO>>> getEvaluations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DoctorEvaluationDTO> result = doctorService.getDoctorEvaluations(id, page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    @PostMapping("/prescription/create")
    public ResponseEntity<Result<PrescriptionDTO>> createPrescription(
            Authentication authentication,
            @Valid @RequestBody PrescriptionCreateDTO dto) {
        Long doctorId = (Long) authentication.getPrincipal();
        PrescriptionDTO result = prescriptionService.createPrescription(dto, doctorId);
        return ResponseEntity.ok(Result.success("处方开具成功", result));
    }
}

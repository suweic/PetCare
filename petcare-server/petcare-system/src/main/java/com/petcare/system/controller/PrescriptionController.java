package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.system.dto.PrescriptionDTO;
import com.petcare.system.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/{consultationId}")
    public ResponseEntity<Result<PrescriptionDTO>> getPrescription(
            Authentication authentication,
            @PathVariable Long consultationId) {
        Long userId = (Long) authentication.getPrincipal();
        PrescriptionDTO result = prescriptionService.getByConsultationId(consultationId, userId);
        return ResponseEntity.ok(Result.success(result));
    }
}

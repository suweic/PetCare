package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.system.dto.PreConsultationRequestDTO;
import com.petcare.system.dto.PreConsultationResultDTO;
import com.petcare.system.service.PreConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pre-consultation")
@RequiredArgsConstructor
public class PreConsultationController {

    private final PreConsultationService preConsultationService;

    /**
     * AI预问诊分析：用户描述宠物症状，LLM自动匹配推荐科室和医生
     */
    @PostMapping("/analyze")
    public ResponseEntity<Result<PreConsultationResultDTO>> analyze(
            Authentication authentication,
            @Valid @RequestBody PreConsultationRequestDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        PreConsultationResultDTO result = preConsultationService.analyze(userId, dto);
        return ResponseEntity.ok(Result.success("AI预问诊分析完成", result));
    }
}

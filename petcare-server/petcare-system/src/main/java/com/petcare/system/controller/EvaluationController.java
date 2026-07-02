package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.system.dto.EvaluationCreateDTO;
import com.petcare.system.dto.EvaluationDTO;
import com.petcare.system.dto.EvaluationReplyDTO;
import com.petcare.system.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/create")
    public ResponseEntity<Result<EvaluationDTO>> createEvaluation(
            Authentication authentication,
            @Valid @RequestBody EvaluationCreateDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        EvaluationDTO result = evaluationService.createEvaluation(dto, userId);
        return ResponseEntity.ok(Result.success("评价成功", result));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<Result<EvaluationDTO>> replyEvaluation(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody EvaluationReplyDTO dto) {
        Long doctorId = (Long) authentication.getPrincipal();
        EvaluationDTO result = evaluationService.replyEvaluation(id, dto, doctorId);
        return ResponseEntity.ok(Result.success("回复成功", result));
    }
}

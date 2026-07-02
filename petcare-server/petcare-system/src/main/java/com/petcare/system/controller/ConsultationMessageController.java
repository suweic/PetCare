package com.petcare.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.dto.ConsultationMessageDTO;
import com.petcare.system.service.ConsultationMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问诊消息控制器 — 提供历史消息查询 API。
 * <p>
 * 新的消息通过 WebSocket 实时推送，此 API 用于加载历史记录。
 * </p>
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
public class ConsultationMessageController {

    private final ConsultationMessageService consultationMessageService;

    /**
     * 查询问诊消息历史（分页，按时间正序）。
     *
     * @param consultationId 问诊ID
     * @param page           页码（默认 1）
     * @param size           每页大小（默认 50，消息通常较多）
     */
    @GetMapping("/{consultationId}/messages")
    public ResponseEntity<Result<Page<ConsultationMessageDTO>>> getMessages(
            Authentication authentication,
            @PathVariable Long consultationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = (Long) authentication.getPrincipal();
        Page<ConsultationMessageDTO> result = consultationMessageService.getMessages(
                consultationId, userId, page, size);
        return ResponseEntity.ok(Result.success(result));
    }
}

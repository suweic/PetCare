package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.system.dto.ChatMessageRequest;
import com.petcare.system.dto.ConsultationMessageDTO;
import com.petcare.system.service.ConsultationMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 问诊消息 WebSocket 控制器 — 处理实时聊天。
 *
 * <h3>消息流</h3>
 * <ol>
 *   <li>客户端 STOMP SEND → {@code /app/chat.send}</li>
 *   <li>服务端保存消息到数据库</li>
 *   <li>通过 {@link SimpMessagingTemplate} 广播到 {@code /topic/consultation/{id}}</li>
 *   <li>问诊双方（用户 + 医生）均收到实时推送</li>
 * </ol>
 *
 * <h3>客户端发送示例</h3>
 * <pre>{@code
 * stompClient.send('/app/chat.send', {}, JSON.stringify({
 *     consultationId: 123,
 *     messageType: 1,
 *     content: "宠物最近食欲不振怎么办？"
 * }));
 * }</pre>
 *
 * <h3>客户端订阅示例</h3>
 * <pre>{@code
 * stompClient.subscribe('/topic/consultation/123', (message) => {
 *     const msg = JSON.parse(message.body);
 *     // msg 为 Result<ConsultationMessageDTO> 格式
 *     renderMessage(msg.data);
 * });
 * }</pre>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConsultationMessageService consultationMessageService;

    /**
     * 处理聊天消息 — 接收、落库、广播。
     *
     * <p>客户端发送到 {@code /app/chat.send}，服务端处理后广播到
     * {@code /topic/consultation/{consultationId}}。</p>
     *
     * @param request       消息请求体
     * @param headerAccessor STOMP 消息头（用于获取 Principal 和 Session 属性）
     * @param principal     当前连接用户（由 {@code JwtHandshakeInterceptor} 设置）
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload @Valid ChatMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor,
                            Principal principal) {

        // 从 Principal 获取发送者 ID（握手时由 JwtHandshakeInterceptor 设置）
        Long senderId = Long.valueOf(principal.getName());

        // 从 Session Attributes 获取用户类型
        Integer userType = (Integer) headerAccessor.getSessionAttributes().get("userType");
        if (userType == null) {
            log.warn("WebSocket 消息缺少 userType: senderId={}", senderId);
            sendError(principal, "认证信息不完整");
            return;
        }

        log.info("收到 WebSocket 消息: consultationId={}, senderId={}, userType={}, messageType={}",
                request.getConsultationId(), senderId, userType, request.getMessageType());

        try {
            // 保存消息到数据库
            ConsultationMessageDTO savedMessage = consultationMessageService.saveMessage(
                    request.getConsultationId(), senderId, userType,
                    request.getMessageType(), request.getContent(),
                    request.getMediaUrl(), request.getDuration());

            // 广播到问诊房间（/topic/consultation/{id}）
            String destination = "/topic/consultation/" + request.getConsultationId();
            messagingTemplate.convertAndSend(destination, Result.success(savedMessage));

            log.debug("消息已广播到 {}: messageId={}", destination, savedMessage.getId());

        } catch (Exception e) {
            log.error("保存问诊消息失败: consultationId={}, senderId={}",
                    request.getConsultationId(), senderId, e);
            sendError(principal, "消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 用户正在输入提示 — 可选功能。
     * <p>客户端发送到 {@code /app/chat.typing}，广播给对方。</p>
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingRequest request, Principal principal) {
        String destination = "/topic/consultation/" + request.getConsultationId();
        messagingTemplate.convertAndSend(destination,
                Result.success(new TypingNotification(principal.getName(), request.isTyping())));
    }

    /**
     * 向指定用户发送错误消息（私有一对一推送）。
     */
    private void sendError(Principal principal, String errorMsg) {
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Result.error(400, errorMsg));
    }

    // ─── 内部类型 ───

    @lombok.Data
    public static class TypingRequest {
        private Long consultationId;
        private boolean typing;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TypingNotification {
        private String userId;
        private boolean typing;
    }
}

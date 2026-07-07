package com.petcare.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * WebSocket 聊天消息请求 — 客户端通过 STOMP SEND 帧发送。
 */
@Data
public class ChatMessageRequest {

    /** 问诊ID */
    @NotNull(message = "问诊ID不能为空")
    private Long consultationId;

    /** 消息类型：1-文本 2-图片 3-语音 4-视频 */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /** 文本内容（messageType=1 时必填） */
    private String content;

    /** 媒体文件URL（messageType=2/3/4 时使用） */
    private String mediaUrl;

    /** 媒体时长（语音/视频，秒） */
    private Integer duration;
}

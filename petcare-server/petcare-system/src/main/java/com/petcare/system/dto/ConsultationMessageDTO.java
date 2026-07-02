package com.petcare.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊消息响应 DTO — 用于历史消息查询 API。
 */
@Data
public class ConsultationMessageDTO {

    private Long id;
    private Long consultationId;

    /** 发送者类型：1-用户 2-医生 */
    private Integer senderType;

    /** 发送者ID */
    private Long senderId;

    /** 消息类型：1-文本 2-图片 3-语音 4-视频 */
    private Integer messageType;

    private String content;
    private String mediaUrl;
    private Integer duration;
    private Integer isRead;
    private LocalDateTime createTime;
}

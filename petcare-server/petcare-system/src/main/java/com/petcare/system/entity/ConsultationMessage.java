package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 问诊消息实体 — 存储问诊过程中的聊天消息（图文/语音/视频）。
 */
@Getter
@Setter
@TableName("consultation_message")
public class ConsultationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId;

    /** 发送者类型：1-用户 2-医生 */
    private Integer senderType;

    /** 发送者ID */
    private Long senderId;

    /** 用户ID（senderType=1时） */
    private Long userId;

    /** 医生ID（senderType=2时） */
    private Long doctorId;

    /** 消息类型：1-文本 2-图片 3-语音 4-视频 */
    private Integer messageType;

    private String content;

    /** 媒体文件URL（图片/语音/视频消息） */
    private String mediaUrl;

    /** 媒体时长（语音/视频消息，秒） */
    private Integer duration;

    /** 是否已读：0-未读 1-已读 */
    private Integer isRead;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}

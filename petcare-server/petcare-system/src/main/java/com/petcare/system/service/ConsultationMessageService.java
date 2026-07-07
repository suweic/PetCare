package com.petcare.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.dto.ConsultationMessageDTO;

public interface ConsultationMessageService {

    /**
     * 分页查询问诊消息历史。
     *
     * @param consultationId 问诊ID
     * @param userId         请求用户ID（用于权限校验）
     * @param page           页码
     * @param size           每页大小
     * @return 消息分页
     */
    Page<ConsultationMessageDTO> getMessages(Long consultationId, Long userId, int page, int size);

    /**
     * 保存 WebSocket 实时消息并返回 DTO，供广播使用。
     *
     * @param consultationId 问诊ID
     * @param senderId       发送者ID
     * @param senderType     发送者类型（1-用户 2-医生）
     * @param messageType    消息类型（1-文本 2-图片 3-语音 4-视频）
     * @param content        文本内容
     * @param mediaUrl       媒体URL（可选）
     * @param duration       媒体时长（可选）
     * @return 保存后的消息 DTO
     */
    ConsultationMessageDTO saveMessage(Long consultationId, Long senderId, Integer senderType,
                                       Integer messageType, String content,
                                       String mediaUrl, Integer duration);
}

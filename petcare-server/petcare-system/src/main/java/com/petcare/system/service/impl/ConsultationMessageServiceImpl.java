package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.ConsultationMessageDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.ConsultationMessage;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.ConsultationMessageMapper;
import com.petcare.system.service.ConsultationMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationMessageServiceImpl implements ConsultationMessageService {

    private final ConsultationMessageMapper messageMapper;
    private final ConsultationMapper consultationMapper;

    @Override
    public Page<ConsultationMessageDTO> getMessages(Long consultationId, Long userId, int page, int size) {
        // 1. 验证问诊存在且属于当前用户（或医生）
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 权限校验：用户本人 或 接诊医生 可查看消息
        boolean isOwner = userId.equals(consultation.getUserId());
        boolean isDoctor = userId.equals(consultation.getDoctorId());
        if (!isOwner && !isDoctor) {
            throw BusinessException.forbidden("无权查看该问诊的消息");
        }

        // 2. 分页查询消息（按时间正序，方便前端渲染对话流）
        Page<ConsultationMessage> pageParam = new Page<>(page, size);
        Page<ConsultationMessage> messagePage = messageMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getConsultationId, consultationId)
                        .orderByAsc(ConsultationMessage::getCreateTime));

        // 3. 转换为 DTO
        List<ConsultationMessageDTO> records = messagePage.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Page<ConsultationMessageDTO> result = new Page<>(page, size);
        result.setTotal(messagePage.getTotal());
        result.setRecords(records);
        result.setPages(messagePage.getPages());
        result.setCurrent(messagePage.getCurrent());
        return result;
    }

    @Override
    public ConsultationMessageDTO saveMessage(Long consultationId, Long senderId, Integer senderType,
                                              Integer messageType, String content,
                                              String mediaUrl, Integer duration) {
        // 1. 验证问诊存在
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 2. 构造实体
        ConsultationMessage message = new ConsultationMessage();
        message.setConsultationId(consultationId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);

        // 根据发送者类型填充 userId 或 doctorId
        if (senderType == 1) {
            message.setUserId(senderId);
        } else if (senderType == 2) {
            message.setDoctorId(senderId);
        }

        message.setMessageType(messageType);
        message.setContent(content);
        message.setMediaUrl(mediaUrl);
        message.setDuration(duration);
        message.setIsRead(0);  // 默认未读

        // 3. 入库
        messageMapper.insert(message);

        log.info("问诊消息已保存: messageId={}, consultationId={}, senderId={}, senderType={}",
                message.getId(), consultationId, senderId, senderType);

        return toDTO(message);
    }

    private ConsultationMessageDTO toDTO(ConsultationMessage m) {
        ConsultationMessageDTO dto = new ConsultationMessageDTO();
        dto.setId(m.getId());
        dto.setConsultationId(m.getConsultationId());
        dto.setSenderType(m.getSenderType());
        dto.setSenderId(m.getSenderId());
        dto.setMessageType(m.getMessageType());
        dto.setContent(m.getContent());
        dto.setMediaUrl(m.getMediaUrl());
        dto.setDuration(m.getDuration());
        dto.setIsRead(m.getIsRead());
        dto.setCreateTime(m.getCreateTime());
        return dto;
    }
}

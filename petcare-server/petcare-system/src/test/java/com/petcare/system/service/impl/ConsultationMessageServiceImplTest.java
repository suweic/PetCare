package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.ConsultationMessageDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.ConsultationMessage;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.ConsultationMessageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ConsultationMessageServiceImpl 单元测试。
 * 覆盖 getMessages 的权限校验、分页查询和 DTO 转换。
 */
@ExtendWith(MockitoExtension.class)
class ConsultationMessageServiceImplTest {

    @Mock
    private ConsultationMessageMapper messageMapper;

    @Mock
    private ConsultationMapper consultationMapper;

    @InjectMocks
    private ConsultationMessageServiceImpl messageService;

    // ==================== 权限校验 ====================

    @Test
    void shouldReturnMessagesForPetOwner() {
        Consultation consultation = createConsultation(1L, 100L, 10L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        Page<ConsultationMessage> mockPage = createMessagePage(1L, 100L);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<ConsultationMessageDTO> result = messageService.getMessages(1L, 100L, 1, 20);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("你好医生", result.getRecords().get(0).getContent());
    }

    @Test
    void shouldReturnMessagesForDoctor() {
        Consultation consultation = createConsultation(1L, 100L, 10L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        Page<ConsultationMessage> mockPage = createMessagePage(1L, 10L);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // 医生（doctorId=10）查看消息
        Page<ConsultationMessageDTO> result = messageService.getMessages(1L, 10L, 1, 20);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
    }

    @Test
    void shouldThrowExceptionWhenConsultationNotFound() {
        when(consultationMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.getMessages(999L, 100L, 1, 20));
        assertEquals("问诊记录不存在", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotAuthorized() {
        Consultation consultation = createConsultation(1L, 100L, 10L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        // 用户200既不是宠物主也不是医生
        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.getMessages(1L, 200L, 1, 20));
        assertEquals("无权查看该问诊的消息", ex.getMessage());
    }

    // ==================== 分页和空结果 ====================

    @Test
    void shouldReturnEmptyPageWhenNoMessages() {
        Consultation consultation = createConsultation(1L, 100L, 10L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        Page<ConsultationMessage> emptyPage = new Page<>(1, 20);
        emptyPage.setRecords(Collections.emptyList());
        emptyPage.setTotal(0);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(emptyPage);

        Page<ConsultationMessageDTO> result = messageService.getMessages(1L, 100L, 1, 20);

        assertTrue(result.getRecords().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    void shouldHandlePaginationCorrectly() {
        Consultation consultation = createConsultation(2L, 200L, 20L);
        when(consultationMapper.selectById(2L)).thenReturn(consultation);

        Page<ConsultationMessage> pageParam = new Page<>(2, 5);
        pageParam.setRecords(List.of(createMessage(5L, 2L, 200L)));
        pageParam.setTotal(11); // 共11条，第2页5条/页
        pageParam.setPages(3);
        pageParam.setCurrent(2);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageParam);

        Page<ConsultationMessageDTO> result = messageService.getMessages(2L, 200L, 2, 5);

        assertEquals(11, result.getTotal());
        assertEquals(3, result.getPages());
        assertEquals(2, result.getCurrent());
        assertEquals(1, result.getRecords().size());
    }

    // ==================== DTO 转换完整性 ====================

    @Test
    void shouldMapAllFieldsToDTO() {
        Consultation consultation = createConsultation(1L, 100L, 10L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        ConsultationMessage msg = new ConsultationMessage();
        msg.setId(10L);
        msg.setConsultationId(1L);
        msg.setSenderType(2); // 医生
        msg.setSenderId(10L);
        msg.setUserId(null);
        msg.setDoctorId(10L);
        msg.setMessageType(2); // 图片
        msg.setContent(null);
        msg.setMediaUrl("https://petcare.com/img/photo.jpg");
        msg.setDuration(null);
        msg.setIsRead(1);
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 14, 30);
        msg.setCreateTime(now);

        Page<ConsultationMessage> pageParam = new Page<>(1, 10);
        pageParam.setRecords(List.of(msg));
        pageParam.setTotal(1);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageParam);

        Page<ConsultationMessageDTO> result = messageService.getMessages(1L, 100L, 1, 10);

        ConsultationMessageDTO dto = result.getRecords().get(0);
        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getConsultationId());
        assertEquals(2, dto.getSenderType());
        assertEquals(10L, dto.getSenderId());
        assertEquals(2, dto.getMessageType());
        assertNull(dto.getContent());
        assertEquals("https://petcare.com/img/photo.jpg", dto.getMediaUrl());
        assertNull(dto.getDuration());
        assertEquals(1, dto.getIsRead());
        assertEquals(now, dto.getCreateTime());
    }

    // ==================== Helpers ====================

    private Consultation createConsultation(Long id, Long userId, Long doctorId) {
        Consultation c = new Consultation();
        c.setId(id);
        c.setUserId(userId);
        c.setDoctorId(doctorId);
        c.setPetId(1L);
        c.setDepartmentId(1L);
        return c;
    }

    private Page<ConsultationMessage> createMessagePage(Long consultationId, Long senderId) {
        ConsultationMessage msg1 = createMessage(1L, consultationId, senderId);
        msg1.setContent("你好医生");
        msg1.setSenderType(1); // 用户
        msg1.setMessageType(1); // 文本

        ConsultationMessage msg2 = createMessage(2L, consultationId, senderId);
        msg2.setContent("你好，请描述症状");
        msg2.setSenderType(2); // 医生
        msg2.setMessageType(1);

        Page<ConsultationMessage> page = new Page<>(1, 20);
        page.setRecords(List.of(msg1, msg2));
        page.setTotal(2);
        page.setCurrent(1);
        page.setPages(1);
        return page;
    }

    private ConsultationMessage createMessage(Long id, Long consultationId, Long senderId) {
        ConsultationMessage msg = new ConsultationMessage();
        msg.setId(id);
        msg.setConsultationId(consultationId);
        msg.setSenderId(senderId);
        msg.setCreateTime(LocalDateTime.now());
        return msg;
    }
}

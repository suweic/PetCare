package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.EvaluationCreateDTO;
import com.petcare.system.dto.EvaluationDTO;
import com.petcare.system.dto.EvaluationReplyDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.Evaluation;
import com.petcare.system.entity.User;
import com.petcare.system.enums.ConsultationStatus;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.EvaluationMapper;
import com.petcare.system.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceImplTest {

    @Mock
    private EvaluationMapper evaluationMapper;
    @Mock
    private ConsultationMapper consultationMapper;
    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    // ===================== CreateEvaluation Tests =====================

    @Test
    void shouldCreateEvaluationSuccessfully() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(evaluationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(evaluationMapper.insert(any(Evaluation.class))).thenAnswer(inv -> {
            Evaluation e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        });

        Evaluation existingEval = new Evaluation();
        existingEval.setId(1L);
        existingEval.setRating(5);
        when(evaluationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(existingEval));

        Doctor doctor = new Doctor();
        doctor.setId(10L);
        doctor.setRating(new BigDecimal("4.8"));
        when(doctorMapper.selectById(10L)).thenReturn(doctor);
        when(doctorMapper.updateById(any(Doctor.class))).thenReturn(1);

        User user = new User();
        user.setId(100L);
        user.setNickname("小王");
        when(userMapper.selectById(100L)).thenReturn(user);

        EvaluationCreateDTO dto = new EvaluationCreateDTO();
        dto.setConsultationId(1L);
        dto.setRating(5);
        dto.setContent("非常专业！");

        EvaluationDTO result = evaluationService.createEvaluation(dto, 100L);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("非常专业！", result.getContent());
        verify(evaluationMapper, times(1)).insert(any(Evaluation.class));
    }

    @Test
    void shouldThrowExceptionWhenConsultationNotFound() {
        when(consultationMapper.selectById(999L)).thenReturn(null);

        EvaluationCreateDTO dto = new EvaluationCreateDTO();
        dto.setConsultationId(999L);
        dto.setRating(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.createEvaluation(dto, 100L));
        assertEquals("问诊记录不存在", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotConsultationOwner() {
        Consultation consultation = createConsultation(1L, 200L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        EvaluationCreateDTO dto = new EvaluationCreateDTO();
        dto.setConsultationId(1L);
        dto.setRating(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.createEvaluation(dto, 100L));
        assertEquals("无权评价该问诊", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConsultationNotCompleted() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        EvaluationCreateDTO dto = new EvaluationCreateDTO();
        dto.setConsultationId(1L);
        dto.setRating(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.createEvaluation(dto, 100L));
        assertEquals("仅可对已完成的问诊进行评价", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAlreadyEvaluated() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(evaluationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        EvaluationCreateDTO dto = new EvaluationCreateDTO();
        dto.setConsultationId(1L);
        dto.setRating(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.createEvaluation(dto, 100L));
        assertEquals("该问诊已评价，不可重复评价", ex.getMessage());
    }

    // ===================== ReplyEvaluation Tests =====================

    @Test
    void shouldReplyEvaluationSuccessfully() {
        Evaluation evaluation = createEvaluation(1L, 10L, "好评");
        when(evaluationMapper.selectById(1L)).thenReturn(evaluation);
        when(evaluationMapper.updateById(any(Evaluation.class))).thenReturn(1);

        User user = new User();
        user.setId(100L);
        user.setNickname("小王");
        when(userMapper.selectById(100L)).thenReturn(user);

        EvaluationReplyDTO replyDTO = new EvaluationReplyDTO();
        replyDTO.setReply("谢谢您的评价！");

        EvaluationDTO result = evaluationService.replyEvaluation(1L, replyDTO, 10L);

        assertNotNull(result);
        assertEquals("谢谢您的评价！", result.getReply());
        verify(evaluationMapper, times(1)).updateById(any(Evaluation.class));
    }

    @Test
    void shouldThrowExceptionWhenReplyEvaluationNotFound() {
        when(evaluationMapper.selectById(999L)).thenReturn(null);

        EvaluationReplyDTO replyDTO = new EvaluationReplyDTO();
        replyDTO.setReply("谢谢！");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.replyEvaluation(999L, replyDTO, 10L));
        assertEquals("评价不存在", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotOwnEvaluation() {
        Evaluation evaluation = createEvaluation(1L, 20L, "好评"); // 属于医生20
        when(evaluationMapper.selectById(1L)).thenReturn(evaluation);

        EvaluationReplyDTO replyDTO = new EvaluationReplyDTO();
        replyDTO.setReply("谢谢！");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.replyEvaluation(1L, replyDTO, 10L));
        assertEquals("无权回复该评价", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAlreadyReplied() {
        Evaluation evaluation = createEvaluation(1L, 10L, "好评");
        evaluation.setReply("已经回复过了");
        when(evaluationMapper.selectById(1L)).thenReturn(evaluation);

        EvaluationReplyDTO replyDTO = new EvaluationReplyDTO();
        replyDTO.setReply("再次回复");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> evaluationService.replyEvaluation(1L, replyDTO, 10L));
        assertEquals("已回复过该评价，不可重复回复", ex.getMessage());
    }

    // ===================== Helpers =====================

    private Consultation createConsultation(Long id, Long userId, Long doctorId, int status) {
        Consultation c = new Consultation();
        c.setId(id);
        c.setUserId(userId);
        c.setDoctorId(doctorId);
        c.setPetId(1L);
        c.setDepartmentId(1L);
        c.setStatus(status);
        return c;
    }

    private Evaluation createEvaluation(Long id, Long doctorId, String content) {
        Evaluation e = new Evaluation();
        e.setId(id);
        e.setConsultationId(1L);
        e.setUserId(100L);
        e.setDoctorId(doctorId);
        e.setRating(5);
        e.setContent(content);
        e.setIsAnonymous(0);
        e.setStatus(1);
        return e;
    }
}

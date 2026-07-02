package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.EvaluationCreateDTO;
import com.petcare.system.dto.EvaluationDTO;
import com.petcare.system.dto.EvaluationReplyDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Evaluation;
import com.petcare.system.entity.User;
import com.petcare.system.enums.ConsultationStatus;
import com.petcare.system.entity.Doctor;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.EvaluationMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价服务实现。
 *
 * TODO: 添加单元测试 — 覆盖 createEvaluation (包括评分更新) / replyEvaluation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final ConsultationMapper consultationMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public EvaluationDTO createEvaluation(EvaluationCreateDTO dto, Long userId) {
        // 1. 查询问诊记录
        Consultation consultation = consultationMapper.selectById(dto.getConsultationId());
        if (consultation == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 2. 验证问诊是否属于当前用户
        if (!userId.equals(consultation.getUserId())) {
            throw BusinessException.forbidden("无权评价该问诊");
        }

        // 3. 验证问诊状态是否为已完成
        if (consultation.getStatus() != ConsultationStatus.COMPLETED.getCode()) {
            throw BusinessException.badRequest("仅可对已完成的问诊进行评价");
        }

        // 4. 验证问诊是否已分配医生
        if (consultation.getDoctorId() == null) {
            throw BusinessException.badRequest("该问诊无接诊医生，无法评价");
        }

        // 5. 检查是否已评价（uk_consultation_id唯一约束）
        Long count = evaluationMapper.selectCount(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getConsultationId, dto.getConsultationId()));
        if (count > 0) {
            throw BusinessException.badRequest("该问诊已评价，不可重复评价");
        }

        // 6. 创建评价（依赖数据库唯一约束防并发重复提交）
        Evaluation evaluation = new Evaluation();
        evaluation.setConsultationId(consultation.getId());
        evaluation.setUserId(userId);
        evaluation.setDoctorId(consultation.getDoctorId());
        evaluation.setRating(dto.getRating());
        evaluation.setContent(dto.getContent());
        evaluation.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        evaluation.setStatus(1); // 默认显示
        try {
            evaluationMapper.insert(evaluation);
        } catch (DuplicateKeyException e) {
            throw BusinessException.badRequest("该问诊已评价，不可重复评价");
        }

        // 更新医生平均评分
        updateDoctorRating(consultation.getDoctorId());

        log.info("评价创建成功: evaluationId={}, consultationId={}, userId={}, rating={}",
                evaluation.getId(), consultation.getId(), userId, dto.getRating());

        return toEvaluationDTO(evaluation);
    }

    /**
     * 重新计算并更新医生的平均评分。
     * <p>
     * 每次评价新增后调用，计算该医生所有有效评价的平均分并更新 doctor.rating。
     * 使用 HALF_UP 舍入模式保留一位小数。
     * </p>
     */
    private void updateDoctorRating(Long doctorId) {
        List<Evaluation> evaluations = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getDoctorId, doctorId)
                        .eq(Evaluation::getStatus, 1));

        if (evaluations.isEmpty()) {
            return;
        }

        double avgRating = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .average()
                .orElse(5.0);

        BigDecimal rating = BigDecimal.valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP);

        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor != null) {
            doctor.setRating(rating);
            doctorMapper.updateById(doctor);
            log.info("医生评分已更新: doctorId={}, newRating={}, evaluationCount={}",
                    doctorId, rating, evaluations.size());
        }
    }

    @Override
    @Transactional
    public EvaluationDTO replyEvaluation(Long evaluationId, EvaluationReplyDTO dto, Long doctorId) {
        // 1. 查询评价
        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw BusinessException.notFound("评价不存在");
        }

        // 2. 验证评价是否属于当前医生
        if (!doctorId.equals(evaluation.getDoctorId())) {
            throw BusinessException.forbidden("无权回复该评价");
        }

        // 3. 验证是否已回复
        if (evaluation.getReply() != null && !evaluation.getReply().isEmpty()) {
            throw BusinessException.badRequest("已回复过该评价，不可重复回复");
        }

        // 4. 更新回复
        evaluation.setReply(dto.getReply());
        evaluation.setReplyTime(LocalDateTime.now());
        evaluationMapper.updateById(evaluation);

        log.info("评价回复成功: evaluationId={}, doctorId={}", evaluationId, doctorId);

        return toEvaluationDTO(evaluation);
    }

    private EvaluationDTO toEvaluationDTO(Evaluation evaluation) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(evaluation.getId());
        dto.setConsultationId(evaluation.getConsultationId());
        dto.setUserId(evaluation.getUserId());
        dto.setDoctorId(evaluation.getDoctorId());
        dto.setRating(evaluation.getRating());
        dto.setContent(evaluation.getContent());
        dto.setIsAnonymous(evaluation.getIsAnonymous());
        dto.setReply(evaluation.getReply());
        dto.setReplyTime(evaluation.getReplyTime());
        dto.setStatus(evaluation.getStatus());
        dto.setCreateTime(evaluation.getCreateTime());

        // 填充用户信息
        if (evaluation.getUserId() != null) {
            User user = userMapper.selectById(evaluation.getUserId());
            if (user != null) {
                boolean anonymous = evaluation.getIsAnonymous() != null && evaluation.getIsAnonymous() == 1;
                dto.setUserName(anonymous ? "匿名用户" : user.getNickname());
                dto.setUserAvatar(anonymous ? null : user.getAvatar());
            }
        }

        return dto;
    }
}

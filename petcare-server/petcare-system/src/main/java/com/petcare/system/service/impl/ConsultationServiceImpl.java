package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Doctor;
import com.petcare.system.enums.ConsultationStatus;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问诊服务实现。
 *
 * 覆盖问诊的完整生命周期：创建 → 待接单 → 进行中 → 已完成/已取消。
 * 权限模型：宠物主（userId）和接诊医生（doctorId）均可查看问诊详情。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationMapper consultationMapper;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional
    public Consultation create(Long userId, Long petId, Long departmentId,
                               Integer type, String chiefComplaint, String symptoms) {
        Consultation c = new Consultation();
        c.setUserId(userId);
        c.setPetId(petId);
        c.setDepartmentId(departmentId);
        c.setType(type);
        c.setChiefComplaint(chiefComplaint);
        c.setSymptoms(symptoms);
        c.setStatus(ConsultationStatus.PENDING.getCode()); // 待接单

        consultationMapper.insert(c);
        log.info("问诊创建成功: consultationId={}, userId={}, deptId={}",
                c.getId(), userId, departmentId);
        return c;
    }

    @Override
    public Page<Consultation> listByUser(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getUserId, userId);
        if (status != null) {
            wrapper.eq(Consultation::getStatus, status);
        }
        wrapper.orderByDesc(Consultation::getCreateTime);
        return consultationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Consultation> listByDoctor(Long doctorUserId, Integer status, int page, int size) {
        // doctorUserId 来自 JWT（User 表 ID），需映射到 Doctor 表 ID
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, doctorUserId));
        if (doctor == null) {
            throw BusinessException.notFound("医生信息不存在");
        }

        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getDoctorId, doctor.getId());
        if (status != null) {
            wrapper.eq(Consultation::getStatus, status);
        }
        wrapper.orderByDesc(Consultation::getCreateTime);
        return consultationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Consultation getDetail(Long consultationId, Long userId) {
        Consultation c = consultationMapper.selectById(consultationId);
        if (c == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 权限校验：先检查宠物主身份
        boolean isOwner = userId.equals(c.getUserId());

        // 再检查医生身份（需通过 Doctor 表映射 userId → doctorId）
        boolean isDoctor = false;
        if (!isOwner && c.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(c.getDoctorId());
            isDoctor = doctor != null && userId.equals(doctor.getUserId());
        }

        if (!isOwner && !isDoctor) {
            throw BusinessException.forbidden("无权查看该问诊记录");
        }

        return c;
    }

    @Override
    @Transactional
    public void cancel(Long consultationId, Long userId) {
        Consultation c = consultationMapper.selectById(consultationId);
        if (c == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 授权校验：仅宠物主本人可取消
        if (!userId.equals(c.getUserId())) {
            throw BusinessException.forbidden("无权取消该问诊");
        }

        // 状态校验：仅"待接单"或"进行中"可取消
        int currentStatus = c.getStatus();
        if (currentStatus != ConsultationStatus.PENDING.getCode()
                && currentStatus != ConsultationStatus.IN_PROGRESS.getCode()) {
            throw BusinessException.badRequest("当前状态不可取消（仅待接单/进行中可取消）");
        }

        c.setStatus(ConsultationStatus.CANCELLED.getCode());
        consultationMapper.updateById(c);
        log.info("问诊已取消: consultationId={}, userId={}", consultationId, userId);
    }
}

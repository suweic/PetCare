package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.PrescriptionCreateDTO;
import org.springframework.dao.DuplicateKeyException;
import com.petcare.system.dto.PrescriptionDTO;
import com.petcare.system.dto.PrescriptionItemDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.Prescription;
import com.petcare.system.entity.PrescriptionItem;
import com.petcare.system.enums.ConsultationStatus;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.PrescriptionItemMapper;
import com.petcare.system.mapper.PrescriptionMapper;
import com.petcare.system.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处方服务实现。
 *
 * 覆盖 createPrescription（含重复开具保护）和 getByConsultationId。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final ConsultationMapper consultationMapper;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional
    public PrescriptionDTO createPrescription(PrescriptionCreateDTO dto, Long doctorId) {
        // 1. 查询问诊记录
        Consultation consultation = consultationMapper.selectById(dto.getConsultationId());
        if (consultation == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 2. 验证问诊是否属于当前医生
        if (!doctorId.equals(consultation.getDoctorId())) {
            throw BusinessException.forbidden("无权为该问诊开具处方");
        }

        // 3. 验证问诊状态是否为进行中
        if (consultation.getStatus() != ConsultationStatus.IN_PROGRESS.getCode()) {
            throw BusinessException.badRequest("当前问诊状态不允许开具处方");
        }

        // 4. 创建处方（依赖数据库 uk_consultation_id 唯一约束防止重复开具）
        Prescription prescription = new Prescription();
        prescription.setConsultationId(consultation.getId());
        prescription.setUserId(consultation.getUserId());
        prescription.setDoctorId(doctorId);
        prescription.setPetId(consultation.getPetId());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setAdvice(dto.getAdvice());
        prescription.setStatus(1); // 已开具
        try {
            prescriptionMapper.insert(prescription);
        } catch (DuplicateKeyException e) {
            throw BusinessException.badRequest("该问诊已开具处方，不可重复开具");
        }

        // 5. 批量创建处方明细
        List<PrescriptionItem> items = dto.getItems().stream().map(itemDTO -> {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription.getId());
            item.setMedicineId(itemDTO.getMedicineId());
            item.setMedicineName(itemDTO.getMedicineName());
            item.setSpecification(itemDTO.getSpecification());
            item.setDosage(itemDTO.getDosage());
            item.setFrequency(itemDTO.getFrequency());
            item.setDuration(itemDTO.getDuration());
            item.setQuantity(itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 1);
            item.setRemarks(itemDTO.getRemarks());
            return item;
        }).collect(Collectors.toList());

        for (PrescriptionItem item : items) {
            prescriptionItemMapper.insert(item);
        }

        // 6. 更新问诊状态为已完成
        consultation.setStatus(ConsultationStatus.COMPLETED.getCode());
        consultationMapper.updateById(consultation);

        // 7. 更新医生问诊次数
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor != null) {
            doctor.setConsultationCount(
                    (doctor.getConsultationCount() != null ? doctor.getConsultationCount() : 0) + 1);
            doctorMapper.updateById(doctor);
        }

        log.info("处方开具成功: prescriptionId={}, consultationId={}, doctorId={}",
                prescription.getId(), consultation.getId(), doctorId);

        return toPrescriptionDTO(prescription, items);
    }

    @Override
    public PrescriptionDTO getByConsultationId(Long consultationId, Long userId) {
        // 查询问诊记录以获取参与方信息
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw BusinessException.notFound("问诊记录不存在");
        }

        // 授权校验：仅允许问诊的宠物主或医生查看处方
        if (!userId.equals(consultation.getUserId()) && !userId.equals(consultation.getDoctorId())) {
            throw BusinessException.forbidden("无权查看该处方的详细信息");
        }

        Prescription prescription = prescriptionMapper.selectOne(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getConsultationId, consultationId));
        if (prescription == null) {
            throw BusinessException.notFound("处方不存在");
        }

        List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescription.getId()));

        return toPrescriptionDTO(prescription, items);
    }

    private PrescriptionDTO toPrescriptionDTO(Prescription prescription, List<PrescriptionItem> items) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setConsultationId(prescription.getConsultationId());
        dto.setUserId(prescription.getUserId());
        dto.setDoctorId(prescription.getDoctorId());
        dto.setPetId(prescription.getPetId());
        dto.setDiagnosis(prescription.getDiagnosis());
        dto.setAdvice(prescription.getAdvice());
        dto.setStatus(prescription.getStatus());
        dto.setCreateTime(prescription.getCreateTime());

        List<PrescriptionItemDTO> itemDTOs = items.stream().map(item -> {
            PrescriptionItemDTO itemDTO = new PrescriptionItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setMedicineId(item.getMedicineId());
            itemDTO.setMedicineName(item.getMedicineName());
            itemDTO.setSpecification(item.getSpecification());
            itemDTO.setDosage(item.getDosage());
            itemDTO.setFrequency(item.getFrequency());
            itemDTO.setDuration(item.getDuration());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setRemarks(item.getRemarks());
            return itemDTO;
        }).collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }
}

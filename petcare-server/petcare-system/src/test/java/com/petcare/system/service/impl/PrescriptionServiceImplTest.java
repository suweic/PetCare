package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.PrescriptionCreateDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * PrescriptionServiceImpl 单元测试。
 * 覆盖 createPrescription（含重复开具保护）和 getByConsultationId。
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @Mock
    private ConsultationMapper consultationMapper;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    // ===================== createPrescription Tests =====================

    @Test
    void shouldCreatePrescriptionSuccessfully() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(prescriptionMapper.insert(any(Prescription.class))).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });
        when(prescriptionItemMapper.insert(any(PrescriptionItem.class))).thenReturn(1);
        when(consultationMapper.updateById(any(Consultation.class))).thenReturn(1);

        Doctor doctor = new Doctor();
        doctor.setId(10L);
        doctor.setConsultationCount(5);
        when(doctorMapper.selectById(10L)).thenReturn(doctor);
        when(doctorMapper.updateById(any(Doctor.class))).thenReturn(1);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        PrescriptionDTO result = prescriptionService.createPrescription(dto, 10L);

        assertNotNull(result);
        assertEquals(1L, result.getConsultationId());
        assertEquals(100L, result.getUserId());
        assertEquals("感冒", result.getDiagnosis());
        assertEquals(1, result.getStatus());
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        assertEquals("阿莫西林", result.getItems().get(0).getMedicineName());

        verify(prescriptionMapper, times(1)).insert(any(Prescription.class));
        verify(prescriptionItemMapper, times(2)).insert(any(PrescriptionItem.class));
        verify(consultationMapper, times(1)).updateById(any(Consultation.class));
        verify(doctorMapper, times(1)).updateById(any(Doctor.class));
    }

    @Test
    void shouldThrowExceptionWhenConsultationNotFound() {
        when(consultationMapper.selectById(999L)).thenReturn(null);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(999L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.createPrescription(dto, 10L));
        assertEquals("问诊记录不存在", ex.getMessage());
        verify(prescriptionMapper, never()).insert(any(Prescription.class));
    }

    @Test
    void shouldThrowExceptionWhenNotConsultationOwner() {
        Consultation consultation = createConsultation(1L, 100L, 20L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        // 医生 10 不是该问诊的医生（问诊属于医生 20）
        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.createPrescription(dto, 10L));
        assertEquals("无权为该问诊开具处方", ex.getMessage());
        verify(prescriptionMapper, never()).insert(any(Prescription.class));
    }

    @Test
    void shouldThrowExceptionWhenConsultationStatusNotInProgress() {
        // 已完成的问诊不能开具处方
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.createPrescription(dto, 10L));
        assertEquals("当前问诊状态不允许开具处方", ex.getMessage());
        verify(prescriptionMapper, never()).insert(any(Prescription.class));
    }

    @Test
    void shouldThrowExceptionWhenDuplicatePrescription() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(prescriptionMapper.insert(any(Prescription.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry for uk_consultation_id"));

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.createPrescription(dto, 10L));
        assertEquals("该问诊已开具处方，不可重复开具", ex.getMessage());
        verify(prescriptionMapper, times(1)).insert(any(Prescription.class));
        verify(prescriptionItemMapper, never()).insert(any(PrescriptionItem.class));
    }

    @Test
    void shouldIncrementDoctorConsultationCount() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(prescriptionMapper.insert(any(Prescription.class))).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });
        when(prescriptionItemMapper.insert(any(PrescriptionItem.class))).thenReturn(1);
        when(consultationMapper.updateById(any(Consultation.class))).thenReturn(1);

        Doctor doctor = new Doctor();
        doctor.setId(10L);
        doctor.setConsultationCount(3);
        when(doctorMapper.selectById(10L)).thenReturn(doctor);
        when(doctorMapper.updateById(Mockito.<Doctor>argThat(d -> d.getConsultationCount() != null && d.getConsultationCount() == 4)))
                .thenReturn(1);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        prescriptionService.createPrescription(dto, 10L);

        verify(doctorMapper).updateById(Mockito.<Doctor>argThat(d -> d.getConsultationCount() == 4));
    }

    @Test
    void shouldHandleNullConsultationCount() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(prescriptionMapper.insert(any(Prescription.class))).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });
        when(prescriptionItemMapper.insert(any(PrescriptionItem.class))).thenReturn(1);
        when(consultationMapper.updateById(any(Consultation.class))).thenReturn(1);

        Doctor doctor = new Doctor();
        doctor.setId(10L);
        doctor.setConsultationCount(null); // null consultation count
        when(doctorMapper.selectById(10L)).thenReturn(doctor);
        when(doctorMapper.updateById(any(Doctor.class))).thenReturn(1);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        prescriptionService.createPrescription(dto, 10L);

        verify(doctorMapper).updateById(Mockito.<Doctor>argThat(d -> d.getConsultationCount() == 1));
    }

    @Test
    void shouldSetDefaultQuantityWhenNull() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.IN_PROGRESS.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(prescriptionMapper.insert(any(Prescription.class))).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });
        when(prescriptionItemMapper.insert(any(PrescriptionItem.class))).thenReturn(1);
        when(consultationMapper.updateById(any(Consultation.class))).thenReturn(1);

        Doctor doctor = new Doctor();
        doctor.setId(10L);
        doctor.setConsultationCount(1);
        when(doctorMapper.selectById(10L)).thenReturn(doctor);
        when(doctorMapper.updateById(any(Doctor.class))).thenReturn(1);

        PrescriptionCreateDTO dto = createPrescriptionCreateDTO(1L);
        // 第二个药品的 quantity 为 null
        dto.getItems().get(1).setQuantity(null);

        PrescriptionDTO result = prescriptionService.createPrescription(dto, 10L);

        assertNotNull(result);
        // 第一个有指定数量
        assertEquals(2, result.getItems().get(0).getQuantity());
        // 第二个默认为 1
        assertEquals(1, result.getItems().get(1).getQuantity());
    }

    // ===================== getByConsultationId Tests =====================

    @Test
    void shouldGetPrescriptionByConsultationIdAsPetOwner() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        Prescription prescription = createPrescription(1L, 1L, 100L, 10L);
        when(prescriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(prescription);

        PrescriptionItem item1 = createPrescriptionItem(1L, 1L, "阿莫西林");
        PrescriptionItem item2 = createPrescriptionItem(2L, 1L, "维生素C");
        when(prescriptionItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(item1, item2));

        // 宠物主（userId=100）查看
        PrescriptionDTO result = prescriptionService.getByConsultationId(1L, 100L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("感冒", result.getDiagnosis());
        assertEquals(2, result.getItems().size());
        verify(prescriptionMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void shouldGetPrescriptionByConsultationIdAsDoctor() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        Prescription prescription = createPrescription(1L, 1L, 100L, 10L);
        when(prescriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(prescription);

        when(prescriptionItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        // 医生（doctorId=10）查看
        PrescriptionDTO result = prescriptionService.getByConsultationId(1L, 10L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenConsultationNotFoundForGet() {
        when(consultationMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.getByConsultationId(999L, 100L));
        assertEquals("问诊记录不存在", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotAuthorizedToView() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);

        // 用户 200 既不是宠物主也不是医生
        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.getByConsultationId(1L, 200L));
        assertEquals("无权查看该处方的详细信息", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPrescriptionNotFound() {
        Consultation consultation = createConsultation(1L, 100L, 10L, ConsultationStatus.COMPLETED.getCode());
        when(consultationMapper.selectById(1L)).thenReturn(consultation);
        when(prescriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> prescriptionService.getByConsultationId(1L, 100L));
        assertEquals("处方不存在", ex.getMessage());
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

    private PrescriptionCreateDTO createPrescriptionCreateDTO(Long consultationId) {
        PrescriptionCreateDTO dto = new PrescriptionCreateDTO();
        dto.setConsultationId(consultationId);
        dto.setDiagnosis("感冒");
        dto.setAdvice("多喝水，注意休息");

        PrescriptionItemDTO item1 = new PrescriptionItemDTO();
        item1.setMedicineId(1L);
        item1.setMedicineName("阿莫西林");
        item1.setSpecification("0.5g*12片");
        item1.setDosage("1片");
        item1.setFrequency("每日3次");
        item1.setDuration("7天");
        item1.setQuantity(2);
        item1.setRemarks("饭后服用");

        PrescriptionItemDTO item2 = new PrescriptionItemDTO();
        item2.setMedicineId(2L);
        item2.setMedicineName("维生素C");
        item2.setSpecification("100mg*60片");
        item2.setDosage("2片");
        item2.setFrequency("每日1次");
        item2.setDuration("14天");
        item2.setQuantity(1);

        dto.setItems(List.of(item1, item2));
        return dto;
    }

    private Prescription createPrescription(Long id, Long consultationId, Long userId, Long doctorId) {
        Prescription p = new Prescription();
        p.setId(id);
        p.setConsultationId(consultationId);
        p.setUserId(userId);
        p.setDoctorId(doctorId);
        p.setPetId(1L);
        p.setDiagnosis("感冒");
        p.setAdvice("多喝水");
        p.setStatus(1);
        return p;
    }

    private PrescriptionItem createPrescriptionItem(Long id, Long prescriptionId, String medicineName) {
        PrescriptionItem item = new PrescriptionItem();
        item.setId(id);
        item.setPrescriptionId(prescriptionId);
        item.setMedicineId(id);
        item.setMedicineName(medicineName);
        item.setSpecification("规格");
        item.setDosage("1片");
        item.setFrequency("每日1次");
        item.setDuration("3天");
        item.setQuantity(1);
        return item;
    }
}

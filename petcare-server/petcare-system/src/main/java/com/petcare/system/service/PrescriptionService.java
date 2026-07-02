package com.petcare.system.service;

import com.petcare.system.dto.PrescriptionCreateDTO;
import com.petcare.system.dto.PrescriptionDTO;

public interface PrescriptionService {

    PrescriptionDTO createPrescription(PrescriptionCreateDTO dto, Long doctorId);

    PrescriptionDTO getByConsultationId(Long consultationId, Long userId);
}

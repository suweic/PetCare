package com.petcare.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.dto.DoctorAuditDTO;
import com.petcare.system.dto.DoctorDetailDTO;
import com.petcare.system.dto.DoctorEvaluationDTO;
import com.petcare.system.dto.DoctorListItemDTO;
import com.petcare.system.dto.DoctorPendingDTO;
import com.petcare.system.dto.DoctorRegisterDTO;
import com.petcare.system.dto.DoctorScheduleDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserLoginDTO;

public interface DoctorService {

    LoginResultDTO login(UserLoginDTO dto);

    void register(DoctorRegisterDTO dto);

    Page<DoctorListItemDTO> getDoctorList(Long deptId, String keyword, int page, int size);

    DoctorDetailDTO getDoctorDetail(Long doctorId);

    Page<DoctorScheduleDTO> getDoctorSchedules(Long doctorId, int page, int size);

    Page<DoctorEvaluationDTO> getDoctorEvaluations(Long doctorId, int page, int size);

    Page<DoctorPendingDTO> getPendingList(int page, int size);

    void audit(Long doctorId, DoctorAuditDTO dto, Long auditorId);
}

package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrescriptionDTO {

    private Long id;
    private Long consultationId;
    private Long userId;
    private Long doctorId;
    private Long petId;
    private String diagnosis;
    private String advice;
    private Integer status;
    private LocalDateTime createTime;
    private List<PrescriptionItemDTO> items;
}

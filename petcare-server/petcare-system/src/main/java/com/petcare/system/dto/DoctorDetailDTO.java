package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DoctorDetailDTO {

    private Long id;
    private Long userId;
    private String realName;
    private String avatar;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String title;
    private String specialty;
    private Integer experience;
    private String education;
    private String hospital;
    private String introduction;
    private BigDecimal consultationFee;
    private BigDecimal rating;
    private Integer consultationCount;
    private List<DoctorEvaluationDTO> recentEvaluations;
}

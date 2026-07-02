package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorListItemDTO {

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
    private String hospital;
    private BigDecimal consultationFee;
    private BigDecimal rating;
    private Integer consultationCount;
    private String introduction;
}

package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DoctorPendingDTO {

    private Long id;
    private Long userId;
    private String realName;
    private String phone;
    private String avatar;
    private Long departmentId;
    private String departmentName;
    private String title;
    private String specialty;
    private Integer experience;
    private String education;
    private String hospital;
    private String introduction;
    private BigDecimal consultationFee;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}

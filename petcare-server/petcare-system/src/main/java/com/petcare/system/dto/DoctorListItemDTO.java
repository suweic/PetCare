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
    // ⚠️ 安全：phone 字段在公开列表查询（/api/doctor/list，permitAll）中不返回，
    // 仅通过需要认证的 /api/doctor/{id} 详情接口返回（DoctorDetailDTO）
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

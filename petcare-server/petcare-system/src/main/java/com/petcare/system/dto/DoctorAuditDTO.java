package com.petcare.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorAuditDTO {

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    @NotBlank(message = "审核意见不能为空")
    private String auditComment;
}

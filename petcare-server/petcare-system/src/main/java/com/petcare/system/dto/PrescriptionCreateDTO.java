package com.petcare.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrescriptionCreateDTO {

    @NotNull(message = "问诊ID不能为空")
    private Long consultationId;

    private String diagnosis;

    private String advice;

    @NotEmpty(message = "处方明细不能为空")
    @Valid
    private List<PrescriptionItemDTO> items;
}

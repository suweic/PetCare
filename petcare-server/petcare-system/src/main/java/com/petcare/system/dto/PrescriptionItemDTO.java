package com.petcare.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionItemDTO {

    /** 处方明细 ID（数据库主键，查询时填充，创建时忽略） */
    private Long id;

    private Long medicineId;

    @NotBlank(message = "药品名称不能为空")
    private String medicineName;

    private String specification;

    @NotBlank(message = "用量不能为空")
    private String dosage;

    @NotBlank(message = "频次不能为空")
    private String frequency;

    @NotBlank(message = "用药时长不能为空")
    private String duration;

    private Integer quantity;

    private String remarks;
}

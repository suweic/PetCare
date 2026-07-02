package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("prescription_item")
public class PrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long prescriptionId;

    private Long medicineId;

    private String medicineName;

    private String specification;

    private String dosage;

    private String frequency;

    private String duration;

    private Integer quantity;

    private String remarks;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}

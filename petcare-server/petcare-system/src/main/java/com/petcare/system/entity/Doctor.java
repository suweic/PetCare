package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("doctor")
public class Doctor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long departmentId;

    private String title;

    private String specialty;

    private Integer experience;

    private String education;

    private String hospital;

    private String qualificationCert;

    private String practiceCert;

    private String introduction;

    private BigDecimal consultationFee;

    private BigDecimal rating;

    private Integer consultationCount;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
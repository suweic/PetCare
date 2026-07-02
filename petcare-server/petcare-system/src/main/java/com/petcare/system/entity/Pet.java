package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private Integer species;

    private String breed;

    private Integer gender;

    private LocalDate birthday;

    private java.math.BigDecimal weight;

    private String avatar;

    private String medicalHistory;

    private String allergyInfo;

    private Integer sterilized;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
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
@TableName("pre_consultation")
public class PreConsultation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long petId;

    private Integer species;

    private String breed;

    private Integer ageYears;

    private Integer ageMonths;

    private String symptoms;

    private String symptomDuration;

    private String additionalInfo;

    private String aiAnalysis;

    private Long recommendedDepartmentId;

    private String recommendedDepartmentName;

    /**
     * JSON string of recommended doctors array.
     * Each element: { doctorId, doctorName, title, hospital, specialty, rating, matchReason }
     */
    private String recommendedDoctors;

    private String generalAdvice;

    private String llmModel;

    private Integer llmTokens;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}

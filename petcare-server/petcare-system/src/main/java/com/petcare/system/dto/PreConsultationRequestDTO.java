package com.petcare.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreConsultationRequestDTO {

    /** 宠物ID（可选，已注册宠物可关联） */
    private Long petId;

    /** 物种：1-猫 2-狗 3-其他 */
    private Integer species;

    /** 品种 */
    @Size(max = 50, message = "品种最长50字符")
    private String breed;

    /** 年龄（年） */
    private Integer ageYears;

    /** 年龄（月，用于幼宠） */
    private Integer ageMonths;

    /** 症状描述（必填） */
    @NotBlank(message = "请描述宠物的症状")
    @Size(min = 2, max = 2000, message = "症状描述2-2000字符")
    private String symptoms;

    /** 症状持续时间，如 "3天"、"1周" */
    @Size(max = 50, message = "持续时间最长50字符")
    private String symptomDuration;

    /** 补充信息（饮食、行为变化、用药史等） */
    @Size(max = 2000, message = "补充信息最长2000字符")
    private String additionalInfo;
}

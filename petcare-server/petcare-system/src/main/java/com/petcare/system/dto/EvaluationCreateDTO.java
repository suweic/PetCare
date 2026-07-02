package com.petcare.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationCreateDTO {

    @NotNull(message = "问诊ID不能为空")
    private Long consultationId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer rating;

    private String content;

    private Integer isAnonymous;
}

package com.petcare.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    private String reply;
}

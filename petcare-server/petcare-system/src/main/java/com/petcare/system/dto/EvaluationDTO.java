package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EvaluationDTO {

    private Long id;
    private Long consultationId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long doctorId;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private String reply;
    private LocalDateTime replyTime;
    private Integer status;
    private LocalDateTime createTime;
}

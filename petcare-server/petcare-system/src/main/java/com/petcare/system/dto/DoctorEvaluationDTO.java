package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DoctorEvaluationDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}

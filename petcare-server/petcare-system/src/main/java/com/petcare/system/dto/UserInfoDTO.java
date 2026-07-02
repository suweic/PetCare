package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDTO {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String realName;
    private Integer userType;
}
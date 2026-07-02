package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResultDTO {

    private String token;
    private UserInfoDTO user;
}
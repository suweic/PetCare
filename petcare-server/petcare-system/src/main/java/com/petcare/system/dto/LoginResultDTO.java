package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResultDTO {

    private String token;
    private UserInfoDTO user;

    /**
     * 是否需要强制修改密码。
     * <p>
     * 管理员首次登录（使用默认密码）时该字段为 true，
     * 前端应弹出密码修改对话框，拒绝修改则强制退出。
     * 普通用户/医生登录时始终为 false。
     * </p>
     */
    private Boolean mustChangePassword = false;
}
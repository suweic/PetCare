package com.petcare.system.service;

import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.PhoneLoginDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.dto.UserRegisterDTO;

public interface UserService {

    LoginResultDTO login(UserLoginDTO dto);

    UserInfoDTO register(UserRegisterDTO dto);

    UserInfoDTO getUserInfo(Long userId);

    /**
     * 发送短信验证码
     * @param phone 手机号
     */
    void sendVerificationCode(String phone);

    /**
     * 手机验证码登录（用户不存在则自动注册）
     * @param dto 手机号 + 验证码
     * @return 登录结果（含JWT token和用户信息）
     */
    LoginResultDTO loginByCode(PhoneLoginDTO dto);
}
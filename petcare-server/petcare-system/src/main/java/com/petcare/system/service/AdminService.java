package com.petcare.system.service;

import com.petcare.system.dto.AdminLoginDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserInfoDTO;

public interface AdminService {

    LoginResultDTO login(AdminLoginDTO dto);

    /**
     * 获取当前管理员信息
     */
    UserInfoDTO getAdminInfo(Long adminId);
}

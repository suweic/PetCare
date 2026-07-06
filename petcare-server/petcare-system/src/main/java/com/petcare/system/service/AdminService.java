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

    /**
     * 修改管理员密码（用于首次登录强制修改 + 主动修改）。
     *
     * @param adminId       管理员ID
     * @param oldPassword   旧密码
     * @param newPassword   新密码（至少8位）
     */
    void changePassword(Long adminId, String oldPassword, String newPassword);
}

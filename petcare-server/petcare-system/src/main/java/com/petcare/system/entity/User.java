package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.system.handler.EncryptedStringTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String password;

    private String nickname;

    private String avatar;

    private String realName;

    /** 身份证号 — 使用 AES-256-GCM 加密存储，满足个人信息保护合规要求 */
    @TableField(typeHandler = EncryptedStringTypeHandler.class)
    private String idCard;

    private Integer userType;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
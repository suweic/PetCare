package com.petcare.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {

    CUSTOMER(1, "普通用户"),
    DOCTOR(2, "医生"),
    ADMIN(3, "管理员");

    private final int code;
    private final String description;

    public static UserType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

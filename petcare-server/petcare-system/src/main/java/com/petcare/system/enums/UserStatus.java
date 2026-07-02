package com.petcare.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "正常");

    private final int code;
    private final String description;
}

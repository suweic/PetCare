package com.petcare.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DoctorStatus {

    PENDING(0, "待审核"),
    ENABLED(1, "正常"),
    DISABLED(2, "禁用"),
    REJECTED(3, "已拒绝");

    private final int code;
    private final String description;
}

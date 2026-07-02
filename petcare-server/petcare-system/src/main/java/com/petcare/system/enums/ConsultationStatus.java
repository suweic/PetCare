package com.petcare.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConsultationStatus {

    PENDING(0, "待接单"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消"),
    REJECTED(4, "已拒绝"),
    TIMEOUT(5, "超时");

    private final int code;
    private final String description;

    public static ConsultationStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConsultationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

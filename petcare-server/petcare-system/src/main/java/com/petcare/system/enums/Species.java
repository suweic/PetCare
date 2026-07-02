package com.petcare.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Species {

    CAT(1, "猫"),
    DOG(2, "狗"),
    OTHER(3, "其他");

    private final int code;
    private final String description;

    private static final Set<Integer> VALID_CODES = Arrays.stream(values())
            .map(Species::getCode)
            .collect(Collectors.toSet());

    public static boolean isValid(Integer code) {
        return code != null && VALID_CODES.contains(code);
    }

    public static Species of(Integer code) {
        if (code == null) {
            return null;
        }
        for (Species s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}

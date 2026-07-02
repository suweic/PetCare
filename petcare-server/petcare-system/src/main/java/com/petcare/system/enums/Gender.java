package com.petcare.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE(1, "公"),
    FEMALE(2, "母");

    private final int code;
    private final String description;

    private static final Set<Integer> VALID_CODES = Arrays.stream(values())
            .map(Gender::getCode)
            .collect(Collectors.toSet());

    public static boolean isValid(Integer code) {
        return code != null && VALID_CODES.contains(code);
    }

    public static Gender of(Integer code) {
        if (code == null) {
            return null;
        }
        for (Gender g : values()) {
            if (g.code == code) {
                return g;
            }
        }
        return null;
    }
}

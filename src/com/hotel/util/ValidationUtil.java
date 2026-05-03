package com.hotel.util;

import java.util.List;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static void requireInOptions(String fieldName, String value, List<String> options) {
        if (!options.contains(value)) {
            throw new IllegalArgumentException(fieldName + "不合法，可选值：" + options);
        }
    }
}

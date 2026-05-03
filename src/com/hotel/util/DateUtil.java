package com.hotel.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    private DateUtil() {
    }

    public static long countNights(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}

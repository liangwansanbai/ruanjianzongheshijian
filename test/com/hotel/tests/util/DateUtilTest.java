package com.hotel.tests.util;

import com.hotel.testframework.Assert;
import com.hotel.testframework.TestCase;
import com.hotel.util.DateUtil;

import java.time.LocalDate;

public class DateUtilTest {

    @TestCase
    public void countNightsShouldReturnDateDifference() {
        long nights = DateUtil.countNights(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        Assert.equals(2L, nights, "住宿天数计算不正确");
    }

    @TestCase
    public void countNightsShouldReturnZeroForSameDay() {
        long nights = DateUtil.countNights(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 1));
        Assert.equals(0L, nights, "同一天的住宿天数应为 0");
    }
}

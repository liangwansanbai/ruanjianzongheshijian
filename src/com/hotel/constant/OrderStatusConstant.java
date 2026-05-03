package com.hotel.constant;

import java.util.Arrays;
import java.util.List;

public final class OrderStatusConstant {
    public static final String PENDING = "待确认";
    public static final String CONFIRMED = "已确认";
    public static final String COMPLETED = "已完成";
    public static final String CANCELED = "已取消";
    public static final List<String> ALL = Arrays.asList(PENDING, CONFIRMED, COMPLETED, CANCELED);

    private OrderStatusConstant() {
    }
}

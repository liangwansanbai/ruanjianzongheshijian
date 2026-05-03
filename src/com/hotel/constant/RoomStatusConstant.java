package com.hotel.constant;

import java.util.Arrays;
import java.util.List;

public final class RoomStatusConstant {
    public static final String AVAILABLE = "空闲";
    public static final String BOOKED = "已预订";
    public static final String MAINTENANCE = "维修中";
    public static final List<String> ALL = Arrays.asList(AVAILABLE, BOOKED, MAINTENANCE);

    private RoomStatusConstant() {
    }
}

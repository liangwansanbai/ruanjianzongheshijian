package com.hotel.tests.support;

import com.hotel.entity.OrderDetail;
import com.hotel.entity.Room;
import com.hotel.entity.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user(int userId, String username, String password, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(username);
        user.setRole(role);
        return user;
    }

    public static Room room(int roomId, String roomNo, String roomType, BigDecimal price, String status, String remark) {
        Room room = new Room();
        room.setRoomId(roomId);
        room.setRoomNo(roomNo);
        room.setRoomType(roomType);
        room.setPrice(price);
        room.setStatus(status);
        room.setRemark(remark);
        return room;
    }

    public static OrderDetail order(int orderId, int userId, int roomId, LocalDate startDate, LocalDate endDate,
                                    BigDecimal totalPrice, String status) {
        OrderDetail order = new OrderDetail();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setRoomId(roomId);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        order.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return order;
    }
}

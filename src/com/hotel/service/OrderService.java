package com.hotel.service;

import com.hotel.constant.OrderStatusConstant;
import com.hotel.constant.RoomStatusConstant;
import com.hotel.dao.OrderDao;
import com.hotel.dao.RoomDao;
import com.hotel.entity.OrderDetail;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.util.DateUtil;
import com.hotel.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OrderService {
    private final OrderDao orderDao;
    private final RoomDao roomDao;

    public OrderService() {
        this(new OrderDao(), new RoomDao());
    }

    public OrderService(OrderDao orderDao, RoomDao roomDao) {
        this.orderDao = orderDao;
        this.roomDao = roomDao;
    }

    public boolean bookRoom(User user, int roomId, LocalDate startDate, LocalDate endDate) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        validateBookingDates(startDate, endDate);

        Room room = roomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (!RoomStatusConstant.AVAILABLE.equals(room.getStatus())) {
            throw new IllegalArgumentException("该房间当前不可预订");
        }
        if (orderDao.hasActiveOrderConflict(roomId, Date.valueOf(startDate), Date.valueOf(endDate))) {
            throw new IllegalArgumentException("该时间段内房间已存在有效订单，请选择其他日期或房间");
        }

        long nights = DateUtil.countNights(startDate, endDate);
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));
        return orderDao.createOrder(
                user.getUserId(),
                roomId,
                Date.valueOf(startDate),
                Date.valueOf(endDate),
                totalPrice
        );
    }

    public List<OrderDetail> getMyOrders(int userId) throws SQLException {
        return orderDao.findByUserId(userId);
    }

    public List<OrderDetail> getAllOrders(String status) throws SQLException {
        String normalizedStatus = normalize(status);
        if (normalizedStatus != null) {
            ValidationUtil.requireInOptions("订单状态", normalizedStatus, OrderStatusConstant.ALL);
        }
        return orderDao.findAll(normalizedStatus);
    }

    public boolean cancelMyOrder(User user, int orderId) throws SQLException {
        OrderDetail order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("只能取消自己的订单");
        }
        if (OrderStatusConstant.COMPLETED.equals(order.getStatus()) || OrderStatusConstant.CANCELED.equals(order.getStatus())) {
            throw new IllegalArgumentException("该订单当前不能取消");
        }
        return orderDao.updateStatus(orderId, OrderStatusConstant.CANCELED);
    }

    public boolean confirmOrder(int orderId) throws SQLException {
        return updateOrderStatus(orderId, OrderStatusConstant.PENDING, OrderStatusConstant.CONFIRMED);
    }

    public boolean completeOrder(int orderId) throws SQLException {
        OrderDetail order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!OrderStatusConstant.CONFIRMED.equals(order.getStatus())) {
            throw new IllegalArgumentException("只有已确认订单才能完成");
        }
        return orderDao.updateStatus(orderId, OrderStatusConstant.COMPLETED);
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        OrderDetail order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (OrderStatusConstant.COMPLETED.equals(order.getStatus()) || OrderStatusConstant.CANCELED.equals(order.getStatus())) {
            throw new IllegalArgumentException("该订单当前不能取消");
        }
        return orderDao.updateStatus(orderId, OrderStatusConstant.CANCELED);
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        OrderDetail order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!OrderStatusConstant.CANCELED.equals(order.getStatus())) {
            throw new IllegalArgumentException("建议只删除已取消订单");
        }
        return orderDao.delete(orderId);
    }

    public List<String[]> orderStatusStats() throws SQLException {
        return orderDao.countByStatus();
    }

    public BigDecimal totalIncome() throws SQLException {
        return orderDao.getTotalIncome();
    }

    private boolean updateOrderStatus(int orderId, String sourceStatus, String targetStatus) throws SQLException {
        OrderDetail order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!sourceStatus.equals(order.getStatus())) {
            throw new IllegalArgumentException("订单状态不符合操作要求");
        }
        return orderDao.updateStatus(orderId, targetStatus);
    }

    private void validateBookingDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("入住日期和离店日期不能为空");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("离店日期必须晚于入住日期");
        }
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}

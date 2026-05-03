package com.hotel.tests.support.fake;

import com.hotel.dao.OrderDao;
import com.hotel.entity.OrderDetail;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeOrderDao extends OrderDao {
    private final Map<Integer, OrderDetail> orders = new LinkedHashMap<Integer, OrderDetail>();
    private int nextId = 1;

    public void addOrder(OrderDetail order) {
        OrderDetail copy = copy(order);
        if (copy.getOrderId() <= 0) {
            copy.setOrderId(nextId++);
        } else if (copy.getOrderId() >= nextId) {
            nextId = copy.getOrderId() + 1;
        }
        if (copy.getCreateTime() == null) {
            copy.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        orders.put(copy.getOrderId(), copy);
    }

    @Override
    public boolean createOrder(int userId, int roomId, Date startDate, Date endDate, BigDecimal totalPrice) throws SQLException {
        OrderDetail order = new OrderDetail();
        order.setOrderId(nextId++);
        order.setUserId(userId);
        order.setRoomId(roomId);
        order.setStartDate(startDate.toLocalDate());
        order.setEndDate(endDate.toLocalDate());
        order.setTotalPrice(totalPrice);
        order.setStatus("待确认");
        order.setCreateTime(new Timestamp(System.currentTimeMillis()));
        orders.put(order.getOrderId(), order);
        return true;
    }

    @Override
    public boolean hasActiveOrderConflict(int roomId, Date startDate, Date endDate) throws SQLException {
        LocalDate start = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();
        for (OrderDetail order : orders.values()) {
            if (order.getRoomId() != roomId) {
                continue;
            }
            if (!"待确认".equals(order.getStatus()) && !"已确认".equals(order.getStatus())) {
                continue;
            }
            if (start.isBefore(order.getEndDate()) && end.isAfter(order.getStartDate())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<OrderDetail> findByUserId(int userId) throws SQLException {
        List<OrderDetail> result = new ArrayList<OrderDetail>();
        for (OrderDetail order : orders.values()) {
            if (order.getUserId() == userId) {
                result.add(copy(order));
            }
        }
        return result;
    }

    @Override
    public List<OrderDetail> findAll(String status) throws SQLException {
        List<OrderDetail> result = new ArrayList<OrderDetail>();
        for (OrderDetail order : orders.values()) {
            if (status == null || status.equals(order.getStatus())) {
                result.add(copy(order));
            }
        }
        return result;
    }

    @Override
    public OrderDetail findById(int orderId) throws SQLException {
        OrderDetail order = orders.get(orderId);
        return order == null ? null : copy(order);
    }

    @Override
    public boolean updateStatus(int orderId, String status) throws SQLException {
        OrderDetail order = orders.get(orderId);
        if (order == null) {
            return false;
        }
        order.setStatus(status);
        return true;
    }

    @Override
    public boolean delete(int orderId) throws SQLException {
        return orders.remove(orderId) != null;
    }

    @Override
    public List<String[]> countByStatus() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (OrderDetail order : orders.values()) {
            counts.put(order.getStatus(), counts.getOrDefault(order.getStatus(), 0) + 1);
        }
        List<String[]> result = new ArrayList<String[]>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            result.add(new String[]{entry.getKey(), String.valueOf(entry.getValue())});
        }
        return result;
    }

    @Override
    public BigDecimal getTotalIncome() throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail order : orders.values()) {
            if ("已完成".equals(order.getStatus()) && order.getTotalPrice() != null) {
                total = total.add(order.getTotalPrice());
            }
        }
        return total;
    }

    public int getOrderCount() {
        return orders.size();
    }

    private OrderDetail copy(OrderDetail source) {
        OrderDetail order = new OrderDetail();
        order.setOrderId(source.getOrderId());
        order.setUserId(source.getUserId());
        order.setUsername(source.getUsername());
        order.setRealName(source.getRealName());
        order.setRoomId(source.getRoomId());
        order.setRoomNo(source.getRoomNo());
        order.setRoomType(source.getRoomType());
        order.setRoomPrice(source.getRoomPrice());
        order.setStartDate(source.getStartDate());
        order.setEndDate(source.getEndDate());
        order.setTotalPrice(source.getTotalPrice());
        order.setStatus(source.getStatus());
        order.setCreateTime(source.getCreateTime());
        return order;
    }
}

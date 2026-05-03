package com.hotel.dao;

import com.hotel.entity.OrderDetail;
import com.hotel.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public boolean createOrder(int userId, int roomId, Date startDate, Date endDate, BigDecimal totalPrice) throws SQLException {
        String sql = "INSERT INTO orders (user_id, room_id, start_date, end_date, total_price, status) "
                + "VALUES (?, ?, ?, ?, ?, '待确认')";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, roomId);
            statement.setDate(3, startDate);
            statement.setDate(4, endDate);
            statement.setBigDecimal(5, totalPrice);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean hasActiveOrderConflict(int roomId, Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders "
                + "WHERE room_id = ? "
                + "AND status IN ('待确认', '已确认') "
                + "AND ? < end_date "
                + "AND ? > start_date";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setDate(2, startDate);
            statement.setDate(3, endDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    public List<OrderDetail> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM v_order_detail WHERE user_id = ? ORDER BY order_id DESC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrderList(resultSet);
            }
        }
    }

    public List<OrderDetail> findAll(String status) throws SQLException {
        String sql = "SELECT * FROM v_order_detail WHERE (? IS NULL OR status = ?) ORDER BY order_id DESC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrderList(resultSet);
            }
        }
    }

    public OrderDetail findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM v_order_detail WHERE order_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOrder(resultSet);
                }
                return null;
            }
        }
    }

    public boolean updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<String[]> countByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS total FROM orders GROUP BY status ORDER BY status";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String[]> result = new ArrayList<String[]>();
            while (resultSet.next()) {
                result.add(new String[]{resultSet.getString("status"), String.valueOf(resultSet.getInt("total"))});
            }
            return result;
        }
    }

    public BigDecimal getTotalIncome() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_price), 0) AS total_income FROM orders WHERE status = '已完成'";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBigDecimal("total_income");
            }
            return BigDecimal.ZERO;
        }
    }

    private List<OrderDetail> mapOrderList(ResultSet resultSet) throws SQLException {
        List<OrderDetail> orders = new ArrayList<OrderDetail>();
        while (resultSet.next()) {
            orders.add(mapOrder(resultSet));
        }
        return orders;
    }

    private OrderDetail mapOrder(ResultSet resultSet) throws SQLException {
        OrderDetail order = new OrderDetail();
        order.setOrderId(resultSet.getInt("order_id"));
        order.setUserId(resultSet.getInt("user_id"));
        order.setUsername(resultSet.getString("username"));
        order.setRealName(resultSet.getString("real_name"));
        order.setRoomId(resultSet.getInt("room_id"));
        order.setRoomNo(resultSet.getString("room_no"));
        order.setRoomType(resultSet.getString("room_type"));
        order.setRoomPrice(resultSet.getBigDecimal("price"));
        order.setStartDate(resultSet.getDate("start_date").toLocalDate());
        order.setEndDate(resultSet.getDate("end_date").toLocalDate());
        order.setTotalPrice(resultSet.getBigDecimal("total_price"));
        order.setStatus(resultSet.getString("status"));
        order.setCreateTime(resultSet.getTimestamp("create_time"));
        return order;
    }
}

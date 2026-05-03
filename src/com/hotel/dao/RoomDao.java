package com.hotel.dao;

import com.hotel.entity.Room;
import com.hotel.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    public List<Room> findByConditions(String roomType, BigDecimal minPrice, BigDecimal maxPrice, String status, String keyword)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT room_id, room_no, room_type, price, status, remark FROM rooms WHERE 1 = 1"
        );
        List<Object> parameters = new ArrayList<Object>();

        if (notBlank(roomType)) {
            sql.append(" AND room_type = ?");
            parameters.add(roomType);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            parameters.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            parameters.add(maxPrice);
        }
        if (notBlank(status)) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }
        if (notBlank(keyword)) {
            sql.append(" AND (room_no LIKE ? OR NVL(remark, '') LIKE ?)");
            String fuzzy = "%" + keyword + "%";
            parameters.add(fuzzy);
            parameters.add(fuzzy);
        }

        sql.append(" ORDER BY room_id");

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            fillParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Room> rooms = new ArrayList<Room>();
                while (resultSet.next()) {
                    rooms.add(mapRoom(resultSet));
                }
                return rooms;
            }
        }
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT room_id, room_no, room_type, price, status, remark FROM rooms WHERE room_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRoom(resultSet);
                }
                return null;
            }
        }
    }

    public boolean existsByRoomNo(String roomNo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_no = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    public boolean existsByRoomNoExcludeId(String roomNo, int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_no = ? AND room_id <> ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomNo);
            statement.setInt(2, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    public boolean insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_id, room_no, room_type, price, status, remark) VALUES (seq_rooms.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, room.getRoomNo());
            statement.setString(2, room.getRoomType());
            statement.setBigDecimal(3, room.getPrice());
            statement.setString(4, room.getStatus());
            statement.setString(5, room.getRemark());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_no = ?, room_type = ?, price = ?, status = ?, remark = ? WHERE room_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, room.getRoomNo());
            statement.setString(2, room.getRoomType());
            statement.setBigDecimal(3, room.getPrice());
            statement.setString(4, room.getStatus());
            statement.setString(5, room.getRemark());
            statement.setInt(6, room.getRoomId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            return statement.executeUpdate() > 0;
        }
    }

    public int countOrdersByRoomId(int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE room_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }

    public List<String[]> countByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS total FROM rooms GROUP BY status ORDER BY status";
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

    private Room mapRoom(ResultSet resultSet) throws SQLException {
        Room room = new Room();
        room.setRoomId(resultSet.getInt("room_id"));
        room.setRoomNo(resultSet.getString("room_no"));
        room.setRoomType(resultSet.getString("room_type"));
        room.setPrice(resultSet.getBigDecimal("price"));
        room.setStatus(resultSet.getString("status"));
        room.setRemark(resultSet.getString("remark"));
        return room;
    }

    private void fillParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

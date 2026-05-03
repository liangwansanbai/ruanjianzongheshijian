package com.hotel.service;

import com.hotel.constant.RoomStatusConstant;
import com.hotel.dao.RoomDao;
import com.hotel.entity.Room;
import com.hotel.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class RoomService {
    private final RoomDao roomDao;

    public RoomService() {
        this(new RoomDao());
    }

    public RoomService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    public List<Room> searchRooms(String roomType, BigDecimal minPrice, BigDecimal maxPrice, String status, String keyword)
            throws SQLException {
        String normalizedStatus = normalize(status);
        if (normalizedStatus != null) {
            ValidationUtil.requireInOptions("房间状态", normalizedStatus, RoomStatusConstant.ALL);
        }
        return roomDao.findByConditions(normalize(roomType), minPrice, maxPrice, normalizedStatus, normalize(keyword));
    }

    public Room getRoomById(int roomId) throws SQLException {
        return roomDao.findById(roomId);
    }

    public boolean addRoom(String roomNo, String roomType, BigDecimal price, String status, String remark) throws SQLException {
        validateRoom(roomNo, roomType, price, status);
        if (roomDao.existsByRoomNo(roomNo.trim())) {
            throw new IllegalArgumentException("房间号已存在");
        }
        Room room = new Room();
        room.setRoomNo(roomNo.trim());
        room.setRoomType(roomType.trim());
        room.setPrice(price);
        room.setStatus(status.trim());
        room.setRemark(normalize(remark));
        return roomDao.insert(room);
    }

    public boolean updateRoom(int roomId, String roomNo, String roomType, BigDecimal price, String status, String remark)
            throws SQLException {
        validateRoom(roomNo, roomType, price, status);
        Room existing = roomDao.findById(roomId);
        if (existing == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (roomDao.existsByRoomNoExcludeId(roomNo.trim(), roomId)) {
            throw new IllegalArgumentException("房间号已被其他房间使用");
        }
        existing.setRoomNo(roomNo.trim());
        existing.setRoomType(roomType.trim());
        existing.setPrice(price);
        existing.setStatus(status.trim());
        existing.setRemark(normalize(remark));
        return roomDao.update(existing);
    }

    public boolean deleteRoom(int roomId) throws SQLException {
        Room existing = roomDao.findById(roomId);
        if (existing == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (roomDao.countOrdersByRoomId(roomId) > 0) {
            throw new IllegalArgumentException("该房间已有订单记录，不能直接删除");
        }
        return roomDao.delete(roomId);
    }

    public List<String[]> roomStatusStats() throws SQLException {
        return roomDao.countByStatus();
    }

    private void validateRoom(String roomNo, String roomType, BigDecimal price, String status) {
        if (ValidationUtil.isBlank(roomNo) || ValidationUtil.isBlank(roomType) || ValidationUtil.isBlank(status)) {
            throw new IllegalArgumentException("房间号、房型、状态不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("房价必须大于 0");
        }
        ValidationUtil.requireInOptions("房间状态", status.trim(), RoomStatusConstant.ALL);
    }

    private String normalize(String value) {
        return ValidationUtil.isBlank(value) ? null : value.trim();
    }
}

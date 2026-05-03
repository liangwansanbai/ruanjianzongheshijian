package com.hotel.tests.support.fake;

import com.hotel.dao.RoomDao;
import com.hotel.entity.Room;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeRoomDao extends RoomDao {
    private final Map<Integer, Room> rooms = new LinkedHashMap<Integer, Room>();
    private final Map<Integer, Integer> orderCounts = new HashMap<Integer, Integer>();

    public void addRoom(Room room) {
        rooms.put(room.getRoomId(), copy(room));
    }

    @Override
    public List<Room> findByConditions(String roomType, BigDecimal minPrice, BigDecimal maxPrice, String status, String keyword)
            throws SQLException {
        List<Room> result = new ArrayList<Room>();
        for (Room room : rooms.values()) {
            if (roomType != null && !roomType.equals(room.getRoomType())) {
                continue;
            }
            if (minPrice != null && room.getPrice().compareTo(minPrice) < 0) {
                continue;
            }
            if (maxPrice != null && room.getPrice().compareTo(maxPrice) > 0) {
                continue;
            }
            if (status != null && !status.equals(room.getStatus())) {
                continue;
            }
            if (keyword != null) {
                String remark = room.getRemark() == null ? "" : room.getRemark();
                if (!room.getRoomNo().contains(keyword) && !remark.contains(keyword)) {
                    continue;
                }
            }
            result.add(copy(room));
        }
        return result;
    }

    @Override
    public Room findById(int roomId) throws SQLException {
        Room room = rooms.get(roomId);
        return room == null ? null : copy(room);
    }

    @Override
    public boolean existsByRoomNo(String roomNo) throws SQLException {
        for (Room room : rooms.values()) {
            if (roomNo.equals(room.getRoomNo())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsByRoomNoExcludeId(String roomNo, int roomId) throws SQLException {
        for (Room room : rooms.values()) {
            if (roomNo.equals(room.getRoomNo()) && room.getRoomId() != roomId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean insert(Room room) throws SQLException {
        int nextId = rooms.isEmpty() ? 1 : (rooms.keySet().stream().max(Integer::compareTo).get() + 1);
        Room saved = copy(room);
        saved.setRoomId(nextId);
        rooms.put(nextId, saved);
        return true;
    }

    @Override
    public boolean update(Room room) throws SQLException {
        if (!rooms.containsKey(room.getRoomId())) {
            return false;
        }
        rooms.put(room.getRoomId(), copy(room));
        return true;
    }

    @Override
    public boolean delete(int roomId) throws SQLException {
        return rooms.remove(roomId) != null;
    }

    @Override
    public int countOrdersByRoomId(int roomId) throws SQLException {
        Integer count = orderCounts.get(roomId);
        return count == null ? 0 : count;
    }

    @Override
    public List<String[]> countByStatus() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (Room room : rooms.values()) {
            counts.put(room.getStatus(), counts.getOrDefault(room.getStatus(), 0) + 1);
        }
        List<String[]> result = new ArrayList<String[]>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            result.add(new String[]{entry.getKey(), String.valueOf(entry.getValue())});
        }
        return result;
    }

    public void setOrderCount(int roomId, int count) {
        orderCounts.put(roomId, count);
    }

    public int getRoomCount() {
        return rooms.size();
    }

    private Room copy(Room source) {
        Room room = new Room();
        room.setRoomId(source.getRoomId());
        room.setRoomNo(source.getRoomNo());
        room.setRoomType(source.getRoomType());
        room.setPrice(source.getPrice());
        room.setStatus(source.getStatus());
        room.setRemark(source.getRemark());
        return room;
    }
}

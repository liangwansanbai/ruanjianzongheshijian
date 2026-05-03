package com.hotel.tests.room;

import com.hotel.constant.RoomStatusConstant;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
import com.hotel.testframework.Assert;
import com.hotel.testframework.BeforeEach;
import com.hotel.testframework.TestCase;
import com.hotel.tests.support.TestDataFactory;
import com.hotel.tests.support.fake.FakeRoomDao;

import java.math.BigDecimal;
import java.util.List;

public class RoomServiceTest {
    private FakeRoomDao roomDao;
    private RoomService roomService;

    @BeforeEach
    public void setUp() {
        roomDao = new FakeRoomDao();
        roomDao.addRoom(TestDataFactory.room(1, "101", "单人间", new BigDecimal("188"), RoomStatusConstant.AVAILABLE, "普通单人间"));
        roomDao.addRoom(TestDataFactory.room(2, "201", "双人间", new BigDecimal("288"), RoomStatusConstant.BOOKED, "适合两人入住"));
        roomDao.addRoom(TestDataFactory.room(3, "301", "豪华间", new BigDecimal("388"), RoomStatusConstant.MAINTENANCE, "维修中"));
        roomService = new RoomService(roomDao);
    }

    @TestCase
    public void searchRoomsShouldFilterByTypeAndPrice() throws Exception {
        List<Room> rooms = roomService.searchRooms("单人间", new BigDecimal("100"), new BigDecimal("200"), null, null);
        Assert.equals(1, rooms.size(), "应只查询出一间单人间");
        Assert.equals("101", rooms.get(0).getRoomNo(), "房间号不正确");
    }

    @TestCase
    public void searchRoomsShouldRejectInvalidStatus() {
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        roomService.searchRooms(null, null, null, "可入住", null);
                    }
                },
                "非法房间状态应被拒绝"
        );
    }

    @TestCase
    public void addRoomShouldInsertNewRoom() throws Exception {
        boolean success = roomService.addRoom("401", "豪华间", new BigDecimal("488"), RoomStatusConstant.AVAILABLE, "新增房间");
        Assert.isTrue(success, "新增房间应成功");
        Assert.equals(4, roomDao.getRoomCount(), "新增后房间数量不正确");
        Assert.isTrue(roomDao.existsByRoomNo("401"), "新增房间后应能查询到该房间号");
    }

    @TestCase
    public void addRoomShouldRejectDuplicateRoomNo() {
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        roomService.addRoom("101", "豪华间", new BigDecimal("488"), RoomStatusConstant.AVAILABLE, "重复房间");
                    }
                },
                "重复房间号应被拒绝"
        );
    }

    @TestCase
    public void updateRoomShouldModifyExistingRoom() throws Exception {
        boolean success = roomService.updateRoom(1, "101", "单人间", new BigDecimal("208"), RoomStatusConstant.AVAILABLE, "已升级");
        Assert.isTrue(success, "修改房间应成功");
        Room room = roomDao.findById(1);
        Assert.equals(new BigDecimal("208"), room.getPrice(), "房价修改失败");
        Assert.equals("已升级", room.getRemark(), "备注修改失败");
    }

    @TestCase
    public void deleteRoomShouldRejectRoomWithOrders() {
        FakeRoomDao roomDaoWithOrder = new FakeRoomDao() {
            @Override
            public int countOrdersByRoomId(int roomId) {
                return roomId == 1 ? 2 : 0;
            }
        };
        roomDaoWithOrder.addRoom(TestDataFactory.room(1, "101", "单人间", new BigDecimal("188"), RoomStatusConstant.AVAILABLE, "普通单人间"));
        RoomService service = new RoomService(roomDaoWithOrder);

        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        service.deleteRoom(1);
                    }
                },
                "存在订单记录的房间不应允许删除"
        );
    }

    @TestCase
    public void roomStatusStatsShouldReturnGroupedCounts() throws Exception {
        List<String[]> stats = roomService.roomStatusStats();
        Assert.equals(3, stats.size(), "房间状态统计数量不正确");
    }
}

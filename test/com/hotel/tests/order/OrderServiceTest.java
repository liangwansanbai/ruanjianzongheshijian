package com.hotel.tests.order;

import com.hotel.constant.OrderStatusConstant;
import com.hotel.constant.RoleConstant;
import com.hotel.constant.RoomStatusConstant;
import com.hotel.entity.OrderDetail;
import com.hotel.entity.User;
import com.hotel.service.OrderService;
import com.hotel.testframework.Assert;
import com.hotel.testframework.BeforeEach;
import com.hotel.testframework.TestCase;
import com.hotel.tests.support.TestDataFactory;
import com.hotel.tests.support.fake.FakeOrderDao;
import com.hotel.tests.support.fake.FakeRoomDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderServiceTest {
    private FakeOrderDao orderDao;
    private FakeRoomDao roomDao;
    private OrderService orderService;
    private User user;

    @BeforeEach
    public void setUp() {
        orderDao = new FakeOrderDao();
        roomDao = new FakeRoomDao();
        roomDao.addRoom(TestDataFactory.room(1, "101", "单人间", new BigDecimal("188"), RoomStatusConstant.AVAILABLE, "普通单人间"));
        roomDao.addRoom(TestDataFactory.room(2, "201", "双人间", new BigDecimal("288"), RoomStatusConstant.BOOKED, "已被预订"));
        user = TestDataFactory.user(2, "user01", "123456", RoleConstant.USER);
        orderService = new OrderService(orderDao, roomDao);
    }

    @TestCase
    public void bookRoomShouldCreatePendingOrderAndCalculateTotalPrice() throws Exception {
        boolean success = orderService.bookRoom(user, 1, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12));
        Assert.isTrue(success, "预订应成功");
        Assert.equals(1, orderDao.getOrderCount(), "应新增一条订单");
        OrderDetail order = orderDao.findAll(null).get(0);
        Assert.equals(OrderStatusConstant.PENDING, order.getStatus(), "新订单状态应为待确认");
        Assert.equals(new BigDecimal("376"), order.getTotalPrice(), "总价计算不正确");
    }

    @TestCase
    public void bookRoomShouldRejectNullUser() {
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.bookRoom(null, 1, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12));
                    }
                },
                "未登录用户不应允许预订"
        );
    }

    @TestCase
    public void bookRoomShouldRejectUnavailableRoom() {
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.bookRoom(user, 2, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12));
                    }
                },
                "非空闲房间不应允许预订"
        );
    }

    @TestCase
    public void bookRoomShouldRejectConflictingDates() {
        orderDao.addOrder(TestDataFactory.order(
                1, 9, 1,
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 13),
                new BigDecimal("376"),
                OrderStatusConstant.CONFIRMED
        ));

        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.bookRoom(user, 1, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12));
                    }
                },
                "时间冲突的订单不应允许预订"
        );
    }

    @TestCase
    public void getAllOrdersShouldRejectInvalidStatus() {
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.getAllOrders("处理中");
                    }
                },
                "非法订单状态应被拒绝"
        );
    }

    @TestCase
    public void cancelMyOrderShouldUpdateOwnOrderStatus() throws Exception {
        orderDao.addOrder(TestDataFactory.order(
                1, user.getUserId(), 1,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                new BigDecimal("376"),
                OrderStatusConstant.PENDING
        ));
        boolean success = orderService.cancelMyOrder(user, 1);
        Assert.isTrue(success, "取消自己的订单应成功");
        Assert.equals(OrderStatusConstant.CANCELED, orderDao.findById(1).getStatus(), "订单状态应更新为已取消");
    }

    @TestCase
    public void cancelMyOrderShouldRejectOtherUsersOrder() {
        orderDao.addOrder(TestDataFactory.order(
                1, 999, 1,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                new BigDecimal("376"),
                OrderStatusConstant.PENDING
        ));
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.cancelMyOrder(user, 1);
                    }
                },
                "不能取消他人的订单"
        );
    }

    @TestCase
    public void confirmOrderShouldOnlyAllowPendingOrder() throws Exception {
        orderDao.addOrder(TestDataFactory.order(
                1, user.getUserId(), 1,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                new BigDecimal("376"),
                OrderStatusConstant.PENDING
        ));
        boolean success = orderService.confirmOrder(1);
        Assert.isTrue(success, "待确认订单应可确认");
        Assert.equals(OrderStatusConstant.CONFIRMED, orderDao.findById(1).getStatus(), "订单状态应更新为已确认");
    }

    @TestCase
    public void completeOrderShouldOnlyAllowConfirmedOrder() {
        orderDao.addOrder(TestDataFactory.order(
                1, user.getUserId(), 1,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                new BigDecimal("376"),
                OrderStatusConstant.PENDING
        ));
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.completeOrder(1);
                    }
                },
                "只有已确认订单才能完成"
        );
    }

    @TestCase
    public void deleteOrderShouldOnlyAllowCanceledOrder() {
        orderDao.addOrder(TestDataFactory.order(
                1, user.getUserId(), 1,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                new BigDecimal("376"),
                OrderStatusConstant.PENDING
        ));
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        orderService.deleteOrder(1);
                    }
                },
                "未取消订单不应允许删除"
        );
    }

    @TestCase
    public void orderStatusStatsAndIncomeShouldBeCalculated() throws Exception {
        orderDao.addOrder(TestDataFactory.order(
                1, user.getUserId(), 1,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 2),
                new BigDecimal("188"),
                OrderStatusConstant.COMPLETED
        ));
        orderDao.addOrder(TestDataFactory.order(
                2, user.getUserId(), 1,
                LocalDate.of(2026, 5, 3),
                LocalDate.of(2026, 5, 4),
                new BigDecimal("188"),
                OrderStatusConstant.CANCELED
        ));

        List<String[]> stats = orderService.orderStatusStats();
        Assert.equals(2, stats.size(), "订单状态统计数量不正确");
        Assert.equals(new BigDecimal("188"), orderService.totalIncome(), "收入统计不正确");
    }
}

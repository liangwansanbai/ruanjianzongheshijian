package com.hotel;

import com.hotel.constant.OrderStatusConstant;
import com.hotel.constant.RoleConstant;
import com.hotel.constant.RoomStatusConstant;
import com.hotel.entity.OrderDetail;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.service.AuthService;
import com.hotel.service.OrderService;
import com.hotel.service.RoomService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = new AuthService();
    private final RoomService roomService = new RoomService();
    private final OrderService orderService = new OrderService();

    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        while (true) {
            printWelcome();
            String choice = scanner.nextLine().trim();
            if ("1".equals(choice)) {
                loginFlow();
            } else if ("0".equals(choice)) {
                System.out.println("系统已退出。");
                break;
            } else {
                System.out.println("输入无效，请重新选择。");
            }
        }
    }

    private void loginFlow() {
        try {
            System.out.print("用户名：");
            String username = scanner.nextLine();
            System.out.print("密码：");
            String password = scanner.nextLine();

            User user = authService.login(username, password);
            if (user == null) {
                System.out.println("登录失败，用户名或密码错误。");
                return;
            }

            System.out.println("登录成功，欢迎你：" + user.getRealName() + "（" + user.getRole() + "）");
            if (RoleConstant.ADMIN.equalsIgnoreCase(user.getRole())) {
                adminMenu(user);
            } else {
                userMenu(user);
            }
        } catch (Exception e) {
            System.out.println("登录异常：" + e.getMessage());
        }
    }

    private void adminMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("===== 管理员菜单 =====");
            System.out.println("1. 查询房间");
            System.out.println("2. 新增房间");
            System.out.println("3. 修改房间");
            System.out.println("4. 删除房间");
            System.out.println("5. 查看全部订单");
            System.out.println("6. 确认订单");
            System.out.println("7. 完成订单");
            System.out.println("8. 取消订单");
            System.out.println("9. 删除订单");
            System.out.println("10. 查看统计");
            System.out.println("0. 退出登录");
            System.out.print("请选择：");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        searchRooms();
                        break;
                    case "2":
                        addRoom();
                        break;
                    case "3":
                        updateRoom();
                        break;
                    case "4":
                        deleteRoom();
                        break;
                    case "5":
                        showAllOrders();
                        break;
                    case "6":
                        handleConfirmOrder();
                        break;
                    case "7":
                        handleCompleteOrder();
                        break;
                    case "8":
                        handleCancelOrder();
                        break;
                    case "9":
                        handleDeleteOrder();
                        break;
                    case "10":
                        showStats();
                        break;
                    case "0":
                        System.out.println("已退出登录。");
                        return;
                    default:
                        System.out.println("输入无效，请重新选择。");
                }
            } catch (Exception e) {
                System.out.println("操作失败：" + e.getMessage());
            }
        }
    }

    private void userMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("===== 用户菜单 =====");
            System.out.println("1. 查询房间");
            System.out.println("2. 查询空闲房间");
            System.out.println("3. 预订房间");
            System.out.println("4. 查看我的订单");
            System.out.println("5. 取消我的订单");
            System.out.println("0. 退出登录");
            System.out.print("请选择：");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        searchRooms();
                        break;
                    case "2":
                        showAvailableRooms();
                        break;
                    case "3":
                        bookRoom(user);
                        break;
                    case "4":
                        showMyOrders(user);
                        break;
                    case "5":
                        cancelMyOrder(user);
                        break;
                    case "0":
                        System.out.println("已退出登录。");
                        return;
                    default:
                        System.out.println("输入无效，请重新选择。");
                }
            } catch (Exception e) {
                System.out.println("操作失败：" + e.getMessage());
            }
        }
    }

    private void searchRooms() throws SQLException {
        System.out.print("房型（可留空）：");
        String roomType = scanner.nextLine();
        BigDecimal minPrice = readOptionalDecimal("最低价格（可留空）：");
        BigDecimal maxPrice = readOptionalDecimal("最高价格（可留空）：");
        System.out.print("状态（" + RoomStatusConstant.ALL + "，可留空）：");
        String status = scanner.nextLine();
        System.out.print("关键字（房间号或备注，可留空）：");
        String keyword = scanner.nextLine();

        List<Room> rooms = roomService.searchRooms(roomType, minPrice, maxPrice, status, keyword);
        printRooms(rooms);
    }

    private void showAvailableRooms() throws SQLException {
        List<Room> rooms = roomService.searchRooms(null, null, null, RoomStatusConstant.AVAILABLE, null);
        printRooms(rooms);
    }

    private void addRoom() throws SQLException {
        String roomNo = readRequired("房间号：");
        String roomType = readRequired("房型：");
        BigDecimal price = readRequiredDecimal("价格：");
        System.out.print("状态（" + RoomStatusConstant.ALL + "）：");
        String status = scanner.nextLine();
        System.out.print("备注：");
        String remark = scanner.nextLine();

        boolean success = roomService.addRoom(roomNo, roomType, price, status, remark);
        System.out.println(success ? "新增房间成功。" : "新增房间失败。");
    }

    private void updateRoom() throws SQLException {
        int roomId = readRequiredInt("房间编号：");
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            System.out.println("房间不存在。");
            return;
        }

        System.out.print("房间号（当前：" + room.getRoomNo() + "）：");
        String roomNo = defaultIfBlank(scanner.nextLine(), room.getRoomNo());
        System.out.print("房型（当前：" + room.getRoomType() + "）：");
        String roomType = defaultIfBlank(scanner.nextLine(), room.getRoomType());
        BigDecimal price = readDecimalWithDefault("价格（当前：" + room.getPrice() + "）：", room.getPrice());
        System.out.print("状态（当前：" + room.getStatus() + "）：");
        String status = defaultIfBlank(scanner.nextLine(), room.getStatus());
        System.out.print("备注（当前：" + safe(room.getRemark()) + "）：");
        String remark = defaultIfBlank(scanner.nextLine(), room.getRemark());

        boolean success = roomService.updateRoom(roomId, roomNo, roomType, price, status, remark);
        System.out.println(success ? "修改房间成功。" : "修改房间失败。");
    }

    private void deleteRoom() throws SQLException {
        int roomId = readRequiredInt("房间编号：");
        boolean success = roomService.deleteRoom(roomId);
        System.out.println(success ? "删除房间成功。" : "删除房间失败。");
    }

    private void bookRoom(User user) throws SQLException {
        showAvailableRooms();
        int roomId = readRequiredInt("请输入要预订的房间编号：");
        LocalDate startDate = readRequiredDate("入住日期（格式：2026-05-10）：");
        LocalDate endDate = readRequiredDate("离店日期（格式：2026-05-12）：");

        boolean success = orderService.bookRoom(user, roomId, startDate, endDate);
        System.out.println(success ? "预订成功，订单状态为待确认。" : "预订失败。");
    }

    private void showMyOrders(User user) throws SQLException {
        List<OrderDetail> orders = orderService.getMyOrders(user.getUserId());
        printOrders(orders);
    }

    private void cancelMyOrder(User user) throws SQLException {
        showMyOrders(user);
        int orderId = readRequiredInt("请输入要取消的订单编号：");
        boolean success = orderService.cancelMyOrder(user, orderId);
        System.out.println(success ? "取消订单成功。" : "取消订单失败。");
    }

    private void showAllOrders() throws SQLException {
        System.out.print("订单状态（" + OrderStatusConstant.ALL + "，可留空）：");
        String status = scanner.nextLine();
        List<OrderDetail> orders = orderService.getAllOrders(status);
        printOrders(orders);
    }

    private void handleConfirmOrder() throws SQLException {
        int orderId = readRequiredInt("订单编号：");
        boolean success = orderService.confirmOrder(orderId);
        System.out.println(success ? "确认订单成功。" : "确认订单失败。");
    }

    private void handleCompleteOrder() throws SQLException {
        int orderId = readRequiredInt("订单编号：");
        boolean success = orderService.completeOrder(orderId);
        System.out.println(success ? "完成订单成功。" : "完成订单失败。");
    }

    private void handleCancelOrder() throws SQLException {
        int orderId = readRequiredInt("订单编号：");
        boolean success = orderService.cancelOrder(orderId);
        System.out.println(success ? "取消订单成功。" : "取消订单失败。");
    }

    private void handleDeleteOrder() throws SQLException {
        int orderId = readRequiredInt("订单编号：");
        boolean success = orderService.deleteOrder(orderId);
        System.out.println(success ? "删除订单成功。" : "删除订单失败。");
    }

    private void showStats() throws SQLException {
        System.out.println("===== 房间状态统计 =====");
        for (String[] item : roomService.roomStatusStats()) {
            System.out.println(item[0] + "：" + item[1]);
        }

        System.out.println("===== 订单状态统计 =====");
        for (String[] item : orderService.orderStatusStats()) {
            System.out.println(item[0] + "：" + item[1]);
        }

        System.out.println("===== 收入统计 =====");
        System.out.println("已完成订单总收入：" + orderService.totalIncome());
    }

    private void printRooms(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("没有查询到房间数据。");
            return;
        }
        System.out.println("房间编号\t房间号\t房型\t价格\t状态\t备注");
        for (Room room : rooms) {
            System.out.println(room.getRoomId() + "\t"
                    + room.getRoomNo() + "\t"
                    + room.getRoomType() + "\t"
                    + room.getPrice() + "\t"
                    + room.getStatus() + "\t"
                    + safe(room.getRemark()));
        }
    }

    private void printOrders(List<OrderDetail> orders) {
        if (orders.isEmpty()) {
            System.out.println("没有查询到订单数据。");
            return;
        }
        System.out.println("订单编号\t用户\t房间号\t房型\t入住日期\t离店日期\t总价\t状态");
        for (OrderDetail order : orders) {
            System.out.println(order.getOrderId() + "\t"
                    + order.getUsername() + "\t"
                    + order.getRoomNo() + "\t"
                    + order.getRoomType() + "\t"
                    + order.getStartDate() + "\t"
                    + order.getEndDate() + "\t"
                    + order.getTotalPrice() + "\t"
                    + order.getStatus());
        }
    }

    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            if (!value.trim().isEmpty()) {
                return value.trim();
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    private int readRequiredInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效整数。");
            }
        }
    }

    private BigDecimal readRequiredDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字。");
            }
        }
    }

    private BigDecimal readOptionalDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字，或者直接回车留空。");
            }
        }
    }

    private BigDecimal readDecimalWithDefault(String prompt, BigDecimal defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字，或者直接回车保留当前值。");
            }
        }
    }

    private LocalDate readRequiredDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("日期格式错误，请使用 yyyy-MM-dd。");
            }
        }
    }

    private String defaultIfBlank(String input, String defaultValue) {
        return input == null || input.trim().isEmpty() ? defaultValue : input.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("===== 酒店管理系统 =====");
        System.out.println("1. 登录系统");
        System.out.println("0. 退出系统");
        System.out.print("请选择：");
    }
}

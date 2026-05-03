package com.hotel.web;

import com.hotel.constant.RoleConstant;
import com.hotel.entity.OrderDetail;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.service.AuthService;
import com.hotel.service.OrderService;
import com.hotel.service.RoomService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class WebServer {
    private final AuthService authService = new AuthService();
    private final RoomService roomService = new RoomService();
    private final OrderService orderService = new OrderService();

    public static void main(String[] args) throws Exception {
        new WebServer().start(8080);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/rooms", new RoomsHandler());
        server.createContext("/api/orders", new OrdersHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Web UI running at http://localhost:" + port);
    }

    private final class StaticHandler extends BaseHandler {
        @Override
        protected void handleRequest(HttpExchange exchange) throws Exception {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                sendResource(exchange, "/com/hotel/web/static/index.html", "text/html; charset=UTF-8");
                return;
            }
            if ("/app.css".equals(path)) {
                sendResource(exchange, "/com/hotel/web/static/app.css", "text/css; charset=UTF-8");
                return;
            }
            if ("/app.js".equals(path)) {
                sendResource(exchange, "/com/hotel/web/static/app.js", "application/javascript; charset=UTF-8");
                return;
            }
            sendJson(exchange, 404, "{\"ok\":false,\"message\":\"Not found\"}");
        }
    }

    private final class LoginHandler extends BaseHandler {
        @Override
        protected void handleRequest(HttpExchange exchange) throws Exception {
            requireMethod(exchange, "POST");
            Map<String, String> form = parseForm(exchange);
            User user = authService.login(form.get("username"), form.get("password"));
            if (user == null) {
                sendError(exchange, 401, "用户名或密码错误");
                return;
            }
            sendJson(exchange, 200, "{\"ok\":true,\"user\":" + toJson(user) + "}");
        }
    }

    private final class RoomsHandler extends BaseHandler {
        @Override
        protected void handleRequest(HttpExchange exchange) throws Exception {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
                List<Room> rooms = roomService.searchRooms(
                        query.get("roomType"),
                        toBigDecimal(query.get("minPrice")),
                        toBigDecimal(query.get("maxPrice")),
                        query.get("status"),
                        query.get("keyword")
                );
                sendJson(exchange, 200, "{\"ok\":true,\"rooms\":" + toJsonRooms(rooms) + "}");
                return;
            }

            requireMethod(exchange, "POST");
            Map<String, String> form = parseForm(exchange);
            String action = form.get("action");
            requireAdmin(form.get("role"));

            if ("create".equals(action)) {
                boolean success = roomService.addRoom(
                        form.get("roomNo"),
                        form.get("roomType"),
                        toRequiredBigDecimal(form.get("price"), "price"),
                        form.get("status"),
                        form.get("remark")
                );
                sendJson(exchange, 200, "{\"ok\":true,\"success\":" + success + "}");
                return;
            }
            if ("update".equals(action)) {
                boolean success = roomService.updateRoom(
                        toRequiredInt(form.get("roomId"), "roomId"),
                        form.get("roomNo"),
                        form.get("roomType"),
                        toRequiredBigDecimal(form.get("price"), "price"),
                        form.get("status"),
                        form.get("remark")
                );
                sendJson(exchange, 200, "{\"ok\":true,\"success\":" + success + "}");
                return;
            }
            if ("delete".equals(action)) {
                boolean success = roomService.deleteRoom(toRequiredInt(form.get("roomId"), "roomId"));
                sendJson(exchange, 200, "{\"ok\":true,\"success\":" + success + "}");
                return;
            }

            sendError(exchange, 400, "未知房间操作");
        }
    }

    private final class OrdersHandler extends BaseHandler {
        @Override
        protected void handleRequest(HttpExchange exchange) throws Exception {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
                String scope = valueOrDefault(query.get("scope"), "mine");
                List<OrderDetail> orders;
                if ("all".equals(scope)) {
                    requireAdmin(query.get("role"));
                    orders = orderService.getAllOrders(query.get("status"));
                } else {
                    orders = orderService.getMyOrders(toRequiredInt(query.get("userId"), "userId"));
                }
                sendJson(exchange, 200, "{\"ok\":true,\"orders\":" + toJsonOrders(orders) + "}");
                return;
            }

            requireMethod(exchange, "POST");
            Map<String, String> form = parseForm(exchange);
            String action = form.get("action");

            if ("book".equals(action)) {
                User user = buildUser(form);
                boolean success = orderService.bookRoom(
                        user,
                        toRequiredInt(form.get("roomId"), "roomId"),
                        LocalDate.parse(required(form.get("startDate"), "startDate")),
                        LocalDate.parse(required(form.get("endDate"), "endDate"))
                );
                sendJson(exchange, 200, "{\"ok\":true,\"success\":" + success + "}");
                return;
            }
            if ("cancelMine".equals(action)) {
                User user = buildUser(form);
                boolean success = orderService.cancelMyOrder(user, toRequiredInt(form.get("orderId"), "orderId"));
                sendJson(exchange, 200, "{\"ok\":true,\"success\":" + success + "}");
                return;
            }

            requireAdmin(form.get("role"));
            boolean success;
            if ("confirm".equals(action)) {
                success = orderService.confirmOrder(toRequiredInt(form.get("orderId"), "orderId"));
            } else if ("complete".equals(action)) {
                success = orderService.completeOrder(toRequiredInt(form.get("orderId"), "orderId"));
            } else if ("cancel".equals(action)) {
                success = orderService.cancelOrder(toRequiredInt(form.get("orderId"), "orderId"));
            } else if ("delete".equals(action)) {
                success = orderService.deleteOrder(toRequiredInt(form.get("orderId"), "orderId"));
            } else {
                sendError(exchange, 400, "未知订单操作");
                return;
            }
            sendJson(exchange, 200, "{\"ok\":true,\"success\":" + success + "}");
        }
    }

    private final class StatsHandler extends BaseHandler {
        @Override
        protected void handleRequest(HttpExchange exchange) throws Exception {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            requireAdmin(query.get("role"));

            List<String[]> roomStats = roomService.roomStatusStats();
            List<String[]> orderStats = orderService.orderStatusStats();
            BigDecimal income = orderService.totalIncome();

            String json = "{\"ok\":true,\"roomStats\":" + toJsonPairs(roomStats)
                    + ",\"orderStats\":" + toJsonPairs(orderStats)
                    + ",\"income\":\"" + escape(income.toPlainString()) + "\"}";
            sendJson(exchange, 200, json);
        }
    }

    private abstract class BaseHandler implements HttpHandler {
        @Override
        public final void handle(HttpExchange exchange) throws IOException {
            try {
                handleRequest(exchange);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (SQLException e) {
                sendError(exchange, 500, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            } finally {
                exchange.close();
            }
        }

        protected abstract void handleRequest(HttpExchange exchange) throws Exception;

        protected void requireMethod(HttpExchange exchange, String method) {
            if (!method.equalsIgnoreCase(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Method not allowed");
            }
        }

        protected void sendResource(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                File file = new File("src" + resourcePath.replace('/', File.separatorChar));
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                }
            }
            if (inputStream == null) {
                sendJson(exchange, 404, "{\"ok\":false,\"message\":\"Missing resource\"}");
                return;
            }
            byte[] body = readAll(inputStream);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType);
            headers.set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        }

        protected void sendJson(HttpExchange exchange, int status, String json) throws IOException {
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "application/json; charset=UTF-8");
            headers.set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        }

        protected void sendError(HttpExchange exchange, int status, String message) throws IOException {
            sendJson(exchange, status, "{\"ok\":false,\"message\":\"" + escape(message) + "\"}");
        }

        protected Map<String, String> parseForm(HttpExchange exchange) throws IOException {
            String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
            return parseQuery(body);
        }
    }

    private User buildUser(Map<String, String> form) {
        User user = new User();
        user.setUserId(toRequiredInt(form.get("userId"), "userId"));
        user.setUsername(valueOrDefault(form.get("username"), ""));
        user.setRealName(valueOrDefault(form.get("realName"), ""));
        user.setRole(valueOrDefault(form.get("role"), ""));
        return user;
    }

    private void requireAdmin(String role) {
        if (!RoleConstant.ADMIN.equals(role)) {
            throw new IllegalArgumentException("该操作仅管理员可用");
        }
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        return value.trim();
    }

    private int toRequiredInt(String value, String field) {
        return Integer.parseInt(required(value, field));
    }

    private BigDecimal toRequiredBigDecimal(String value, String field) {
        return new BigDecimal(required(value, field));
    }

    private BigDecimal toBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private Map<String, String> parseQuery(String query) throws IOException {
        Map<String, String> values = new LinkedHashMap<String, String>();
        if (query == null || query.trim().isEmpty()) {
            return values;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            int index = pair.indexOf('=');
            String key = index >= 0 ? pair.substring(0, index) : pair;
            String value = index >= 0 ? pair.substring(index + 1) : "";
            values.put(
                    URLDecoder.decode(key, "UTF-8"),
                    URLDecoder.decode(value, "UTF-8")
            );
        }
        return values;
    }

    private byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private String toJson(User user) {
        return "{\"userId\":" + user.getUserId()
                + ",\"username\":\"" + escape(user.getUsername()) + "\""
                + ",\"realName\":\"" + escape(user.getRealName()) + "\""
                + ",\"role\":\"" + escape(user.getRole()) + "\"}";
    }

    private String toJsonRooms(List<Room> rooms) {
        List<String> items = new ArrayList<String>();
        for (Room room : rooms) {
            items.add("{\"roomId\":" + room.getRoomId()
                    + ",\"roomNo\":\"" + escape(room.getRoomNo()) + "\""
                    + ",\"roomType\":\"" + escape(room.getRoomType()) + "\""
                    + ",\"price\":\"" + escape(room.getPrice().toPlainString()) + "\""
                    + ",\"status\":\"" + escape(room.getStatus()) + "\""
                    + ",\"remark\":\"" + escape(room.getRemark() == null ? "" : room.getRemark()) + "\"}");
        }
        return "[" + join(items) + "]";
    }

    private String toJsonOrders(List<OrderDetail> orders) {
        List<String> items = new ArrayList<String>();
        for (OrderDetail order : orders) {
            items.add("{\"orderId\":" + order.getOrderId()
                    + ",\"userId\":" + order.getUserId()
                    + ",\"username\":\"" + escape(order.getUsername()) + "\""
                    + ",\"realName\":\"" + escape(order.getRealName()) + "\""
                    + ",\"roomId\":" + order.getRoomId()
                    + ",\"roomNo\":\"" + escape(order.getRoomNo()) + "\""
                    + ",\"roomType\":\"" + escape(order.getRoomType()) + "\""
                    + ",\"roomPrice\":\"" + escape(order.getRoomPrice().toPlainString()) + "\""
                    + ",\"startDate\":\"" + escape(String.valueOf(order.getStartDate())) + "\""
                    + ",\"endDate\":\"" + escape(String.valueOf(order.getEndDate())) + "\""
                    + ",\"totalPrice\":\"" + escape(order.getTotalPrice().toPlainString()) + "\""
                    + ",\"status\":\"" + escape(order.getStatus()) + "\""
                    + ",\"createTime\":\"" + escape(String.valueOf(order.getCreateTime())) + "\"}");
        }
        return "[" + join(items) + "]";
    }

    private String toJsonPairs(List<String[]> pairs) {
        List<String> items = new ArrayList<String>();
        for (String[] pair : pairs) {
            items.add("{\"label\":\"" + escape(pair[0]) + "\",\"value\":\"" + escape(pair[1]) + "\"}");
        }
        return "[" + join(items) + "]";
    }

    private String join(List<String> items) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(items.get(i));
        }
        return builder.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

package com.hotel.config;

public final class DatabaseConfig {

    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String URL = "jdbc:mysql://localhost:3306/hotel?serverTimezone=UTC&useSSL=false&characterEncoding=UTF-8";
    public static final String USERNAME = "hotel";
    public static final String PASSWORD = "hotel";

    private DatabaseConfig() {
    }
}

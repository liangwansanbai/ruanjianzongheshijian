package com.hotel.config;

public final class DatabaseConfig {

    public static final String DRIVER = "oracle.jdbc.OracleDriver";
    public static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    public static final String USERNAME = "hotel";
    public static final String PASSWORD = "hotel";

    private DatabaseConfig() {
    }
}

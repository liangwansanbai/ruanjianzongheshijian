package com.hotel.dao;

import com.hotel.entity.User;
import com.hotel.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public User findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, password, real_name, role FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setUserId(resultSet.getInt("user_id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    user.setRealName(resultSet.getString("real_name"));
                    user.setRole(resultSet.getString("role"));
                    return user;
                }
                return null;
            }
        }
    }
}

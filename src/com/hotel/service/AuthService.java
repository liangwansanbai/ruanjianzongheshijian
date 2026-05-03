package com.hotel.service;

import com.hotel.dao.UserDao;
import com.hotel.entity.User;
import com.hotel.util.ValidationUtil;

import java.sql.SQLException;

public class AuthService {
    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) throws SQLException {
        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        return userDao.findByUsernameAndPassword(username.trim(), password.trim());
    }
}

package com.hotel.tests.support.fake;

import com.hotel.dao.UserDao;
import com.hotel.entity.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FakeUserDao extends UserDao {
    private final Map<String, User> users = new HashMap<String, User>();

    public void addUser(User user) {
        users.put(buildKey(user.getUsername(), user.getPassword()), user);
    }

    @Override
    public User findByUsernameAndPassword(String username, String password) throws SQLException {
        return users.get(buildKey(username, password));
    }

    private String buildKey(String username, String password) {
        return username + "::" + password;
    }
}

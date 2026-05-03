package com.hotel.tests.auth;

import com.hotel.constant.RoleConstant;
import com.hotel.entity.User;
import com.hotel.service.AuthService;
import com.hotel.testframework.Assert;
import com.hotel.testframework.BeforeEach;
import com.hotel.testframework.TestCase;
import com.hotel.tests.support.TestDataFactory;
import com.hotel.tests.support.fake.FakeUserDao;

public class AuthServiceTest {
    private FakeUserDao userDao;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        userDao = new FakeUserDao();
        userDao.addUser(TestDataFactory.user(1, "admin", "123456", RoleConstant.ADMIN));
        authService = new AuthService(userDao);
    }

    @TestCase
    public void loginShouldReturnUserWhenCredentialsMatch() throws Exception {
        User user = authService.login("admin", "123456");
        Assert.notNull(user, "登录成功时应返回用户对象");
        Assert.equals("admin", user.getUsername(), "返回用户名不正确");
    }

    @TestCase
    public void loginShouldTrimInputBeforeQuery() throws Exception {
        User user = authService.login("  admin  ", " 123456 ");
        Assert.notNull(user, "登录前应自动去除首尾空格");
    }

    @TestCase
    public void loginShouldReturnNullWhenCredentialsDoNotMatch() throws Exception {
        User user = authService.login("admin", "wrong");
        Assert.isNull(user, "账号密码错误时应返回 null");
    }

    @TestCase
    public void loginShouldRejectBlankUsername() {
        IllegalArgumentException exception = Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() throws Exception {
                        authService.login(" ", "123456");
                    }
                },
                "空用户名应被拒绝"
        );
        Assert.equals("用户名和密码不能为空", exception.getMessage(), "异常消息不正确");
    }
}

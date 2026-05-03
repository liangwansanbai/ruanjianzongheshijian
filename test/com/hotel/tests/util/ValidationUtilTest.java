package com.hotel.tests.util;

import com.hotel.testframework.Assert;
import com.hotel.testframework.TestCase;
import com.hotel.util.ValidationUtil;

import java.util.Arrays;

public class ValidationUtilTest {

    @TestCase
    public void isBlankShouldReturnTrueForNullAndWhitespace() {
        Assert.isTrue(ValidationUtil.isBlank(null), "null 应被判定为空");
        Assert.isTrue(ValidationUtil.isBlank("   "), "空白字符串应被判定为空");
    }

    @TestCase
    public void isBlankShouldReturnFalseForText() {
        Assert.isFalse(ValidationUtil.isBlank("hotel"), "普通文本不应被判定为空");
    }

    @TestCase
    public void requireInOptionsShouldAllowValidValue() {
        ValidationUtil.requireInOptions("状态", "空闲", Arrays.asList("空闲", "已预订"));
    }

    @TestCase
    public void requireInOptionsShouldRejectInvalidValue() {
        Assert.throwsException(
                IllegalArgumentException.class,
                new Assert.ThrowingRunnable() {
                    @Override
                    public void run() {
                        ValidationUtil.requireInOptions("状态", "维修中", Arrays.asList("空闲", "已预订"));
                    }
                },
                "非法选项应被拒绝"
        );
    }
}

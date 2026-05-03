package com.hotel.testframework;

import java.util.Objects;

public final class Assert {

    private Assert() {
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    public static void equals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + "，期望：" + expected + "，实际：" + actual);
        }
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    public static void isNull(Object value, String message) {
        if (value != null) {
            throw new AssertionError(message + "，实际值：" + value);
        }
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static <T extends Throwable> T throwsException(Class<T> expectedType, ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (expectedType.isInstance(throwable)) {
                return expectedType.cast(throwable);
            }
            throw new AssertionError(message + "，异常类型不匹配，实际：" + throwable.getClass().getName(), throwable);
        }
        throw new AssertionError(message + "，未抛出预期异常：" + expectedType.getName());
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}

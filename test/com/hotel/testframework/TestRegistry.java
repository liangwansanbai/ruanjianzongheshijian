package com.hotel.testframework;

public final class TestRegistry {
    private static final Class<?>[] TEST_CLASSES = new Class<?>[]{
            com.hotel.tests.auth.AuthServiceTest.class,
            com.hotel.tests.room.RoomServiceTest.class,
            com.hotel.tests.order.OrderServiceTest.class,
            com.hotel.tests.util.ValidationUtilTest.class,
            com.hotel.tests.util.DateUtilTest.class
    };

    private TestRegistry() {
    }

    public static Class<?>[] getTestClasses() {
        return TEST_CLASSES.clone();
    }
}

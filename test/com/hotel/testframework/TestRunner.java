package com.hotel.testframework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestRunner {

    public TestSummary runAll() {
        return run(null, null);
    }

    public TestSummary runClass(String simpleClassName) {
        return run(simpleClassName, null);
    }

    public TestSummary runMethod(String simpleClassName, String methodName) {
        return run(simpleClassName, methodName);
    }

    private TestSummary run(String classNameFilter, String methodNameFilter) {
        int passed = 0;
        int failed = 0;
        List<String> failures = new ArrayList<String>();

        for (Class<?> testClass : TestRegistry.getTestClasses()) {
            if (classNameFilter != null && !testClass.getSimpleName().equals(classNameFilter)) {
                continue;
            }

            List<Method> beforeEachMethods = findAnnotatedMethods(testClass, BeforeEach.class);
            List<Method> testMethods = findAnnotatedMethods(testClass, TestCase.class);
            for (Method testMethod : testMethods) {
                if (methodNameFilter != null && !testMethod.getName().equals(methodNameFilter)) {
                    continue;
                }

                String displayName = testClass.getSimpleName() + "#" + testMethod.getName();
                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();
                    for (Method beforeEach : beforeEachMethods) {
                        beforeEach.setAccessible(true);
                        beforeEach.invoke(instance);
                    }

                    testMethod.setAccessible(true);
                    testMethod.invoke(instance);
                    passed++;
                    System.out.println("[PASS] " + displayName);
                } catch (InvocationTargetException e) {
                    failed++;
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    failures.add(displayName + " -> " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                    System.out.println("[FAIL] " + displayName);
                    System.out.println("       " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                } catch (Exception e) {
                    failed++;
                    failures.add(displayName + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    System.out.println("[FAIL] " + displayName);
                    System.out.println("       " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }

        return new TestSummary(passed, failed, failures);
    }

    private List<Method> findAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotationClass) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }
        methods.sort(Comparator.comparing(Method::getName));
        return methods;
    }
}

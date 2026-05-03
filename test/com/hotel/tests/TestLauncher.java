package com.hotel.tests;

import com.hotel.testframework.TestRunner;
import com.hotel.testframework.TestSummary;

public class TestLauncher {

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        TestSummary summary;

        if (args.length == 0 || "all".equalsIgnoreCase(args[0])) {
            summary = runner.runAll();
        } else if (args[0].contains("#")) {
            String[] parts = args[0].split("#", 2);
            summary = runner.runMethod(parts[0], parts[1]);
        } else {
            summary = runner.runClass(args[0]);
        }

        System.out.println();
        System.out.println("===== 测试汇总 =====");
        System.out.println("总数：" + summary.getTotal());
        System.out.println("通过：" + summary.getPassed());
        System.out.println("失败：" + summary.getFailed());
        if (!summary.getFailures().isEmpty()) {
            System.out.println("失败明细：");
            for (String failure : summary.getFailures()) {
                System.out.println(" - " + failure);
            }
        }

        if (summary.getFailed() > 0) {
            System.exit(1);
        }
    }
}

package com.hotel.testframework;

import java.util.List;

public class TestSummary {
    private final int passed;
    private final int failed;
    private final List<String> failures;

    public TestSummary(int passed, int failed, List<String> failures) {
        this.passed = passed;
        this.failed = failed;
        this.failures = failures;
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    public List<String> getFailures() {
        return failures;
    }

    public int getTotal() {
        return passed + failed;
    }
}

package com.xeli.createvoidway.blocks.voidtypes.motor;

/** Dependency-free regression checks; run via the motorStressRegression Gradle task. */
public final class VoidMotorStressBudgetTest {
    public static void main(String[] args) {
        check(256, VoidMotorStressBudget.allocate(256, 0, 0, 320, 320));
        check(192, VoidMotorStressBudget.allocate(256, 64, 0, 320, 320));
        // An existing relay load must not reduce its own budget on the next tick.
        check(256, VoidMotorStressBudget.allocate(256, 256, 256, 320, 320));
        check(192, VoidMotorStressBudget.allocate(256, 256, 192, 320, 320));
        // Two inputs share one source, including when connected to different void channels.
        float first = VoidMotorStressBudget.allocate(256, 0, 0, 320, 640);
        float second = VoidMotorStressBudget.allocate(256, 0, 0, 320, 640);
        check(128, first);
        check(256, first + second);
        check(0, VoidMotorStressBudget.allocate(256, 300, 0, 320, 320));
        check(320, VoidMotorStressBudget.allocate(10240, 0, 0, 320, 320));
        check(0, VoidMotorStressBudget.allocate(0, 0, 0, 320, 320));
        System.out.println("Motor stress allocation regression checks passed.");
    }

    private static void check(float expected, float actual) {
        if (Math.abs(expected - actual) > 0.001f)
            throw new AssertionError("Expected " + expected + " SU, got " + actual);
    }
}

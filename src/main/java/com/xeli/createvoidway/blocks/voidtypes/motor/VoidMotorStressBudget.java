package com.xeli.createvoidway.blocks.voidtypes.motor;

/** Allocates spare source capacity without counting existing relay loads twice. */
public final class VoidMotorStressBudget {
    private VoidMotorStressBudget() {}

    public static float grantRpm(int requested, float available, float totalDemand,
            float stressPerRpm, float maxCapacity) {
        if (available <= 0 || totalDemand <= 0 || stressPerRpm <= 0 || maxCapacity <= 0)
            return 0;
        float scale = Math.min(1, available / totalDemand);
        float magnitude = Math.min(Math.abs((float) requested) * scale, maxCapacity / stressPerRpm);
        return Math.copySign(magnitude, requested);
    }

    public static float allocate(float capacity, float totalStress, float relayStress,
            float inputLimit, float totalInputLimit) {
        if (inputLimit <= 0 || totalInputLimit <= 0)
            return 0;
        float otherStress = Math.max(0, totalStress - relayStress);
        float available = Math.max(0, capacity - otherStress);
        return Math.min(inputLimit, available * (inputLimit / totalInputLimit));
    }
}

package com.xeli.createvoidway.blocks.voidtypes.motor;

/** Allocates spare source capacity without counting existing relay loads twice. */
public final class VoidMotorStressBudget {
    private VoidMotorStressBudget() {}

    public static float outputCapacity(float available, int activeOutputs, float maxCapacity) {
        if (available <= 0 || activeOutputs <= 0 || maxCapacity <= 0)
            return 0;
        return Math.min(maxCapacity, available / activeOutputs);
    }

    public static float outputRpm(int requested, float allocatedCapacity) {
        return allocatedCapacity > 0 ? requested : 0;
    }

    public static float capacityPerRpm(float allocatedCapacity, float rpm) {
        return allocatedCapacity > 0 && rpm != 0 ? allocatedCapacity / Math.abs(rpm) : 0;
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

package com.xeli.createvoidway.config;

public final class VoidChannelStress {

	private VoidChannelStress() {}

	public static int computeDemand(int baseStress, int stressAtFullChannel, float fillRatio) {
		fillRatio = Math.clamp(fillRatio, 0f, 1f);
		if (stressAtFullChannel <= baseStress)
			return baseStress;
		return baseStress + (int) Math.floor(fillRatio * (stressAtFullChannel - baseStress));
	}

	public static float toImpactPerRpm(int channelStressDemand, float speed) {
		if (channelStressDemand <= 0 || speed == 0)
			return 0;
		return channelStressDemand / Math.abs(speed);
	}

}

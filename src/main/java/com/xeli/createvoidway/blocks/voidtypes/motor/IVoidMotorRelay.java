package com.xeli.createvoidway.blocks.voidtypes.motor;

import net.minecraft.world.level.LevelAccessor;

/**
 * Directed void motor nodes on a frequency channel.
 * Inputs contribute stress; outputs draw from the pool up to their requested RPM.
 */
public interface IVoidMotorRelay {

	boolean isVoidMotorOutput();

	VoidMotorLinkBehaviour getVoidMotorLink();

	float getChannelStressContribution();

	default float getRequestedChannelStress() {
		return 0;
	}

	default void applyGrantedSpeed(float rpm) {}

	void clearChannelStress();

	boolean isRelayAlive();

	/**
	 * Local readiness only — must not inspect partners.
	 * Inputs: rotating with fluid. Outputs: always ready while alive.
	 */
	boolean isLocallyReady();

	void setChannelStressStats(float total, float usedStress);

	void updateLinkedPartnerCount(LevelAccessor world);

	int getLinkedPartners();

	void setLinkedPartners(int partners);

	int getReadyPartners();

	void setReadyPartners(int partners);

}

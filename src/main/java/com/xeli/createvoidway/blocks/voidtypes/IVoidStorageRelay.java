package com.xeli.createvoidway.blocks.voidtypes;

import net.minecraft.world.level.LevelAccessor;

public interface IVoidStorageRelay {

	VoidStorageKind getStorageKind();

	boolean isStorageOutput();

	VoidStorageLinkBehaviour getStorageLink();

	int getLinkedPartners();

	void setLinkedPartners(int partners);

	int getReadyPartners();

	void setReadyPartners(int partners);

	/**
	 * Local stress/fluid readiness only — must not inspect partners (avoids recursion).
	 */
	boolean isLocallyReady();

	void updateLinkedPartnerCount(LevelAccessor world);

	boolean isRelayAlive();

}

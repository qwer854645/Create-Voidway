package com.xeli.createvoidway.blocks.voidtypes;

import net.minecraft.world.level.LevelAccessor;

public interface IVoidStorageRelay {

	VoidStorageKind getStorageKind();

	boolean isStorageOutput();

	VoidStorageLinkBehaviour getStorageLink();

	int getLinkedPartners();

	void setLinkedPartners(int partners);

	void updateLinkedPartnerCount(LevelAccessor world);

	boolean isRelayAlive();

}

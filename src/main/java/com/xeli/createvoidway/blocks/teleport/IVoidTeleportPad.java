package com.xeli.createvoidway.blocks.teleport;

import com.xeli.createvoidway.blocks.teleport.VoidTeleportNetworkHandler.PairStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IVoidTeleportPad {

	void setNetworkState(PairStatus status, int padCount,
			@Nullable ResourceLocation partnerDimension, @Nullable BlockPos partner, int linkDistance);

	PairStatus getPairStatus();

	int getPadCount();

	@Nullable
	BlockPos getPartnerPos();

	@Nullable
	ResourceLocation getPartnerDimension();

}

package com.xeli.createvoidway.blocks.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IVoidPortalEndpoint {

	void setNetworkState(VoidPortalNetworkHandler.PairStatus status, int portalCount,
			@Nullable ResourceLocation partnerDimension, @Nullable BlockPos partner, int linkDistance);

	VoidPortalNetworkHandler.PairStatus getPairStatus();

	int getPortalCount();

	@Nullable
	BlockPos getPartnerPos();

	@Nullable
	ResourceLocation getPartnerDimension();

	int getLinkDistance();

}

package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkMetrics;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportNetworkHandler;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface IVoidPortalEndpoint {

	void setNetworkState(VoidPortalNetworkHandler.PairStatus status, int portalCount,
			@Nullable BlockPos partner, int linkDistance);

	VoidPortalNetworkHandler.PairStatus getPairStatus();

	int getPortalCount();

	@Nullable
	BlockPos getPartnerPos();

	int getLinkDistance();

}

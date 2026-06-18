package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkMetrics;

public final class VoidPortalLinkMetrics {

	private VoidPortalLinkMetrics() {}

	public static int computeStressDemand(int distanceBlocks) {
		return VoidTeleportLinkMetrics.computeStressDemand(distanceBlocks);
	}

}

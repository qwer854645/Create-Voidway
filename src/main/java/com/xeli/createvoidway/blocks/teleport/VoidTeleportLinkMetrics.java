package com.xeli.createvoidway.blocks.teleport;

import com.xeli.createvoidway.config.VoidwayConfig;
import net.minecraft.core.BlockPos;

public final class VoidTeleportLinkMetrics {

	private VoidTeleportLinkMetrics() {}

	public static int computeDistanceBlocks(BlockPos from, BlockPos to) {
		double dx = (from.getX() + 0.5) - (to.getX() + 0.5);
		double dy = (from.getY() + 0.5) - (to.getY() + 0.5);
		double dz = (from.getZ() + 0.5) - (to.getZ() + 0.5);
		return Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz)));
	}

	public static int computeRawStressDemand(int distanceBlocks) {
		if (distanceBlocks <= 0)
			return 0;
		return VoidwayConfig.getVoidTeleportStressBase()
				+ VoidwayConfig.getVoidTeleportStressPerBlock() * distanceBlocks;
	}

	public static int computeStressDemand(int distanceBlocks) {
		int raw = computeRawStressDemand(distanceBlocks);
		if (raw <= 0)
			return 0;
		return Math.min(VoidwayConfig.getVoidTeleportStressMax(),
				Math.max(VoidwayConfig.getVoidTeleportStressMin(), raw));
	}

	public static boolean isStressFloored(int distanceBlocks) {
		int raw = computeRawStressDemand(distanceBlocks);
		return raw > 0 && raw < VoidwayConfig.getVoidTeleportStressMin();
	}

	public static boolean isStressCapped(int distanceBlocks) {
		int raw = computeRawStressDemand(distanceBlocks);
		return raw > VoidwayConfig.getVoidTeleportStressMax();
	}

}

package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.compat.VoidwaySableCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class VoidTerminalTeleportLanding {

	private static final double GROUND_Y_OFFSET = 0.05;

	private VoidTerminalTeleportLanding() {
	}

	public static Vec3 findTeleportPos(ServerLevel level, BlockPos target, ServerPlayer player) {
		if (isValidStandingSpot(level, target, player))
			return VoidwaySableCompat.globalTeleportPos(level, target, GROUND_Y_OFFSET);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos candidate = target.relative(direction);
			if (isValidStandingSpot(level, candidate, player))
				return VoidwaySableCompat.globalTeleportPos(level, candidate, GROUND_Y_OFFSET);
		}

		return VoidwaySableCompat.globalTeleportPos(level, target, GROUND_Y_OFFSET);
	}

	private static boolean isValidStandingSpot(ServerLevel level, BlockPos stand, ServerPlayer player) {
		BlockPos floor = stand.below();
		if (!isSolidFloor(level, floor))
			return false;
		if (!level.getFluidState(stand).isEmpty())
			return false;
		return hasEntitySpace(level, stand, player);
	}

	private static boolean isSolidFloor(LevelReader level, BlockPos floor) {
		return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP);
	}

	private static boolean hasEntitySpace(ServerLevel level, BlockPos feetBlock, ServerPlayer player) {
		Vec3 feet = Vec3.atBottomCenterOf(feetBlock).add(0, GROUND_Y_OFFSET, 0);
		AABB box = player.getDimensions(player.getPose()).makeBoundingBox(feet);
		return level.noCollision(player, box);
	}

}

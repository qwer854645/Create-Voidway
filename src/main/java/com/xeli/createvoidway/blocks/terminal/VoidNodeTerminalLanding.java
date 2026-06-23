package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class VoidNodeTerminalLanding {

	private static final double GROUND_Y_OFFSET = 0.05;
	private static final double TOP_Y_OFFSET = 2.05;

	private VoidNodeTerminalLanding() {
	}

	public static Vec3 findTeleportPos(ServerLevel level, BlockPos terminalBase, ServerPlayer player) {
		BlockState terminalState = level.getBlockState(terminalBase);
		for (Direction direction : sidePriority(terminalState)) {
			BlockPos stand = terminalBase.relative(direction);
			if (isValidSideStandingSpot(level, stand, player))
				return VoidwaySableCompat.globalTeleportPos(level, stand, GROUND_Y_OFFSET);
		}
		return VoidwaySableCompat.globalTeleportPos(level, terminalBase, TOP_Y_OFFSET);
	}

	private static Direction[] sidePriority(BlockState terminalState) {
		Direction facing = terminalState.getValue(HorizontalDirectionalBlock.FACING);
		return new Direction[] {
				facing,
				facing.getClockWise(),
				facing.getCounterClockWise(),
				facing.getOpposite()
		};
	}

	private static boolean isValidSideStandingSpot(Level level, BlockPos stand, ServerPlayer player) {
		BlockPos floor = stand.below();
		if (!isSolidFloor(level, floor))
			return false;
		if (isTerminalBlock(level, stand) || isTerminalBlock(level, stand.above()))
			return false;
		if (!level.getFluidState(stand).isEmpty())
			return false;
		return hasEntitySpace(level, stand, player);
	}

	private static boolean isSolidFloor(LevelReader level, BlockPos floor) {
		BlockState state = level.getBlockState(floor);
		return state.isFaceSturdy(level, floor, Direction.UP);
	}

	private static boolean isTerminalBlock(LevelReader level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.is(RWBlocks.VOID_NODE_TERMINAL.get()) || state.is(RWBlocks.VOID_NODE_TERMINAL_TOP.get());
	}

	private static boolean hasEntitySpace(Level level, BlockPos feetBlock, ServerPlayer player) {
		Vec3 feet = Vec3.atBottomCenterOf(feetBlock).add(0, GROUND_Y_OFFSET, 0);
		AABB box = player.getDimensions(player.getPose()).makeBoundingBox(feet);
		return level.noCollision(player, box);
	}

}

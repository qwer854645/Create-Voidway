package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.blocks.RWBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class VoidNodeTerminalMultiblock {

	/** Link slots on the upper block, expressed in voxels from the base block origin. */
	public static final float FREQ_SLOT_Y_OFFSET = 16F;

	private VoidNodeTerminalMultiblock() {}

	public static boolean isBase(BlockState state) {
		return state.is(RWBlocks.VOID_NODE_TERMINAL.get());
	}

	public static boolean isTop(BlockState state) {
		return state.is(RWBlocks.VOID_NODE_TERMINAL_TOP.get());
	}

	@Nullable
	public static BlockPos getBasePos(LevelReader level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (isBase(state))
			return pos;
		if (isTop(state))
			return pos.below();
		return null;
	}

	public static BlockPos getTopPos(BlockPos basePos) {
		return basePos.above();
	}

	public static boolean hasValidTop(LevelReader level, BlockPos basePos) {
		return isTop(level.getBlockState(getTopPos(basePos)));
	}

	@Nullable
	public static VoidNodeTerminalTileEntity getTerminal(LevelAccessor level, BlockPos pos) {
		BlockPos basePos = getBasePos(level, pos);
		if (basePos == null)
			return null;
		if (level.getBlockEntity(basePos) instanceof VoidNodeTerminalTileEntity terminal)
			return terminal;
		return null;
	}

	public static void placeTop(LevelAccessor level, BlockPos basePos, Direction facing) {
		level.setBlock(getTopPos(basePos),
				RWBlocks.VOID_NODE_TERMINAL_TOP.getDefaultState().setValue(VoidNodeTerminalTopBlock.FACING, facing),
				3);
	}

	public static void ensureTop(Level level, BlockPos basePos, Direction facing) {
		if (!level.isClientSide && !hasValidTop(level, basePos))
			placeTop(level, basePos, facing);
	}

	public static void removeTop(Level level, BlockPos basePos) {
		BlockPos topPos = getTopPos(basePos);
		if (isTop(level.getBlockState(topPos)))
			level.removeBlock(topPos, false);
	}

	public static BlockPos resolveBehaviourPos(LevelReader level, BlockPos pos) {
		BlockPos basePos = getBasePos(level, pos);
		return basePos != null ? basePos : pos;
	}

}
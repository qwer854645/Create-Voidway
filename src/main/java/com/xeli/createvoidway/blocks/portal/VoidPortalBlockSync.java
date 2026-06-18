package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.RWBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class VoidPortalBlockSync {

	private VoidPortalBlockSync() {}

	public static void fill(ServerLevel level, VoidPortalShape shape) {
		BlockState portalState = RWBlocks.VOID_PORTAL.getDefaultState()
				.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS,
						shape.walkAxis());
		for (BlockPos pos : shape.iterateInteriorPositions()) {
			BlockState current = level.getBlockState(pos);
			if (current.is(RWBlocks.VOID_PORTAL.get()))
				continue;
			if (VoidPortalShape.isReplaceableByPortal(level, pos))
				level.setBlock(pos, portalState, Block.UPDATE_ALL);
		}
	}

	public static void clear(ServerLevel level, VoidPortalShape shape) {
		for (BlockPos pos : shape.iterateInteriorPositions()) {
			if (level.getBlockState(pos).is(RWBlocks.VOID_PORTAL.get()))
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	public static void onPortalBlockRemoved(Level level, BlockPos pos) {
		if (level.isClientSide)
			return;
		for (Direction dir : Direction.values()) {
			BlockPos neighbor = pos.relative(dir);
			if (level.getBlockEntity(neighbor) instanceof VoidPortalConnectorTileEntity connector)
				connector.refreshPortalBlocks();
		}
	}

}

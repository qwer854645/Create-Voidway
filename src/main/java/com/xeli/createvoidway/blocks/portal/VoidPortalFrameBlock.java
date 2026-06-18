package com.xeli.createvoidway.blocks.portal;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.VoidwayMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoidPortalFrameBlock extends Block {

	public static final MapCodec<VoidPortalFrameBlock> CODEC = simpleCodec(VoidPortalFrameBlock::new);
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

	public VoidPortalFrameBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (!level.isClientSide())
			VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onPortalBlockChanged(level, pos);
		return state;
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!level.isClientSide())
			VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onPortalBlockChanged(level, pos);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide())
			VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onPortalBlockChanged(level, pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

}

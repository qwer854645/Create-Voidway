package com.xeli.createvoidway.blocks.portal;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.xeli.createvoidway.VoidwayMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
public class VoidPortalStressBlock extends HorizontalDirectionalBlock implements IBE<VoidPortalStressTileEntity>, IRotate {

	public static final MapCodec<VoidPortalStressBlock> CODEC = simpleCodec(VoidPortalStressBlock::new);

	public VoidPortalStressBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		Direction front = state.getValue(FACING);
		return face == front || face == front.getOpposite();
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
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

	@Override
	public Class<VoidPortalStressTileEntity> getBlockEntityClass() {
		return VoidPortalStressTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidPortalStressTileEntity> getBlockEntityType() {
		return com.xeli.createvoidway.blocks.RWTileEntities.VOID_PORTAL_STRESS.get();
	}

}

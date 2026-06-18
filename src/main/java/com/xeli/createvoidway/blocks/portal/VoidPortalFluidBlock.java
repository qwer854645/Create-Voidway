package com.xeli.createvoidway.blocks.portal;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import com.xeli.createvoidway.VoidwayMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoidPortalFluidBlock extends Block implements IBE<VoidPortalFluidTileEntity> {

	public static final MapCodec<VoidPortalFluidBlock> CODEC = simpleCodec(VoidPortalFluidBlock::new);
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

	public VoidPortalFluidBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
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

	@Override
	public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (net.neoforged.neoforge.fluids.FluidUtil.interactWithFluidHandler(player, hand, world, pos, hit.getDirection()))
			return ItemInteractionResult.SUCCESS;
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public Class<VoidPortalFluidTileEntity> getBlockEntityClass() {
		return VoidPortalFluidTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidPortalFluidTileEntity> getBlockEntityType() {
		return com.xeli.createvoidway.blocks.RWTileEntities.VOID_PORTAL_FLUID.get();
	}

}

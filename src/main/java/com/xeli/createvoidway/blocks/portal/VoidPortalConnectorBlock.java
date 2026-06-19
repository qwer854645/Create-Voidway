package com.xeli.createvoidway.blocks.portal;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.voidlink.VoidLinkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class VoidPortalConnectorBlock extends HorizontalDirectionalBlock implements IBE<VoidPortalConnectorTileEntity> {

	public static final MapCodec<VoidPortalConnectorBlock> CODEC = simpleCodec(VoidPortalConnectorBlock::new);

	public VoidPortalConnectorBlock(Properties properties) {
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

	/** Face toward the player; matches south-baked slot models with fixed blockstate rotation. */
	public static Direction getLinkSlotFace(BlockState state) {
		return state.getValue(FACING);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (!level.isClientSide()) {
			VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(level, pos, VoidLinkBehaviour.TYPE);
			if (behaviour != null && placer instanceof Player player)
				behaviour.setOwner(player.getGameProfile());
		}
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
		if (!state.is(newState.getBlock()) && !level.isClientSide()) {
			if (level.getBlockEntity(pos) instanceof VoidPortalConnectorTileEntity connector)
				connector.clearPortalBlocksOnRemove();
			VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.removePortalFromAllNetworks(level, pos);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (heldItem.isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		VoidLinkBehaviour link = BlockEntityBehaviour.get(world, pos, VoidLinkBehaviour.TYPE);
		if (link != null) {
			for (int index : VoidLinkHandler.arr012) {
				if (link.testHit(index, hit.getLocation()))
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public Class<VoidPortalConnectorTileEntity> getBlockEntityClass() {
		return VoidPortalConnectorTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidPortalConnectorTileEntity> getBlockEntityType() {
		return com.xeli.createvoidway.blocks.RWTileEntities.VOID_PORTAL_CONNECTOR.get();
	}

}

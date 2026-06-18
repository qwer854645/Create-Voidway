package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.voidlink.VoidLinkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public abstract class AbstractVoidTankBlock<T extends AbstractVoidTankTileEntity> extends Block
		implements SimpleWaterloggedBlock, IWrenchable, IBE<T>, IRotate {

	public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

	protected AbstractVoidTankBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(CLOSED, false).setValue(WATERLOGGED, false));
	}

	protected abstract MapCodec<? extends Block> codecImpl();

	@Override
	protected MapCodec<? extends Block> codec() {
		return codecImpl();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CLOSED, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		return defaultBlockState()
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		if (!level.isClientSide()) {
			VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(level, pos, VoidLinkBehaviour.TYPE);
			if (behaviour != null && placer instanceof Player player)
				behaviour.setOwner(player.getGameProfile());
		}
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(CLOSED, !state.getValue(CLOSED)));
		return InteractionResult.SUCCESS;
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (heldItem.isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!(world.getBlockEntity(pos) instanceof AbstractVoidTankTileEntity))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		VoidLinkBehaviour link = BlockEntityBehaviour.get(world, pos, VoidLinkBehaviour.TYPE);
		if (link != null) {
			for (int index : VoidLinkHandler.arr012) {
				if (link.testHit(index, hit.getLocation()))
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
		}

		if (net.neoforged.neoforge.fluids.FluidUtil.interactWithFluidHandler(player, hand, world, pos, hit.getDirection()))
			return ItemInteractionResult.SUCCESS;

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return Direction.Axis.Y;
	}

	@Override
	public abstract Class<T> getBlockEntityClass();

	@Override
	public abstract BlockEntityType<? extends T> getBlockEntityType();

}

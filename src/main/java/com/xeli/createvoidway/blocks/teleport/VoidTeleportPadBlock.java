package com.xeli.createvoidway.blocks.teleport;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.xeli.createvoidway.VoidwayMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class VoidTeleportPadBlock extends Block implements SimpleWaterloggedBlock, IWrenchable, IBE<VoidTeleportPadTileEntity>, IRotate {

	public static final MapCodec<VoidTeleportPadBlock> CODEC = simpleCodec(VoidTeleportPadBlock::new);

	/** Plate top in block units (5/16 ≈ one third of a block). */
	public static final float PLATE_HEIGHT = 5f / 16f;

	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 5, 15);
	private static final VoxelShape TELEPORT_ZONE = Block.box(1, 5, 1, 15, 16, 15);

	public VoidTeleportPadBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
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
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.or(SHAPE, TELEPORT_ZONE);
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
		if (!level.isClientSide())
			VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.validatePadBinding(level, pos);
		return state;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return VoidTeleportWrenchHandler.onWrenchedPad(context);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		return defaultBlockState()
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!level.isClientSide())
			VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.validatePadBinding(level, pos);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide())
			VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.removePadFromAllNetworks(level, pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (heldItem.isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!(world.getBlockEntity(pos) instanceof VoidTeleportPadTileEntity))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

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
	public Class<VoidTeleportPadTileEntity> getBlockEntityClass() {
		return VoidTeleportPadTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidTeleportPadTileEntity> getBlockEntityType() {
		return com.xeli.createvoidway.blocks.RWTileEntities.VOID_TELEPORT_PAD.get();
	}

}

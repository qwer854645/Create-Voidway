package com.xeli.createvoidway.blocks.terminal;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.xeli.createvoidway.blocks.RWBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VoidNodeTerminalTopBlock extends HorizontalDirectionalBlock implements IWrenchable {

	public static final MapCodec<VoidNodeTerminalTopBlock> CODEC = simpleCodec(VoidNodeTerminalTopBlock::new);
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

	public VoidNodeTerminalTopBlock(Properties properties) {
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
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return null;
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
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return VoidNodeTerminalMultiblock.isBase(level.getBlockState(pos.below()));
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (direction == Direction.DOWN && !VoidNodeTerminalMultiblock.isBase(neighbourState))
			return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
		return state;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide) {
			BlockPos below = pos.below();
			if (VoidNodeTerminalMultiblock.isBase(level.getBlockState(below)))
				level.destroyBlock(below, true);
		}
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return redirectWrench(context, false);
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		return redirectWrench(context, true);
	}

	private InteractionResult redirectWrench(UseOnContext context, boolean sneak) {
		Level level = context.getLevel();
		BlockPos basePos = context.getClickedPos().below();
		BlockState baseState = level.getBlockState(basePos);
		if (!(baseState.getBlock() instanceof IWrenchable wrenchable))
			return InteractionResult.PASS;

		UseOnContext redirected = new UseOnContext(level, context.getPlayer(), context.getHand(),
				context.getItemInHand(),
				new BlockHitResult(context.getClickLocation(), context.getClickedFace(), basePos, context.isInside()));
		InteractionResult result = sneak ? wrenchable.onSneakWrenched(baseState, redirected)
				: wrenchable.onWrenched(baseState, redirected);
		if (result.consumesAction() && !sneak && !level.isClientSide) {
			BlockState updatedBase = level.getBlockState(basePos);
			if (VoidNodeTerminalMultiblock.isBase(updatedBase))
				VoidNodeTerminalMultiblock.placeTop(level, basePos, updatedBase.getValue(FACING));
		}
		return result;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos base = pos.below();
		return RWBlocks.VOID_NODE_TERMINAL.get().useItemOn(stack, level.getBlockState(base), level, base, player, hand,
				hitResult);
	}

	@Override
	protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hitResult) {
		BlockPos base = pos.below();
		return RWBlocks.VOID_NODE_TERMINAL.get().useWithoutItem(level.getBlockState(base), level, base, player,
				hitResult);
	}

}

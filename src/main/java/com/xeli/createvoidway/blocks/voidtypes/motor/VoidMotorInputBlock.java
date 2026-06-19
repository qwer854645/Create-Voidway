package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VoidMotorInputBlock extends AbstractVoidMotorBlock<VoidMotorInputTileEntity> {

	public static final MapCodec<VoidMotorInputBlock> CODEC = simpleCodec(VoidMotorInputBlock::new);

	public VoidMotorInputBlock(Properties properties) {
		super(properties);
	}

	/** ValueBox orientation; matches north-baked slot models (Create Utilities void motor). */
	public static Direction getLinkSlotFace(BlockState state) {
		return state.getValue(DirectionalKineticBlock.FACING);
	}

	@Override
	protected MapCodec<? extends DirectionalKineticBlock> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidMotorInputTileEntity> getBlockEntityClass() {
		return VoidMotorInputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidMotorInputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_MOTOR_INPUT.get();
	}

}

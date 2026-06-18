package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidMotorOutputBlock extends AbstractVoidMotorBlock<VoidMotorOutputTileEntity> {

	public static final MapCodec<VoidMotorOutputBlock> CODEC = simpleCodec(VoidMotorOutputBlock::new);

	public VoidMotorOutputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends DirectionalKineticBlock> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidMotorOutputTileEntity> getBlockEntityClass() {
		return VoidMotorOutputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidMotorOutputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_MOTOR_OUTPUT.get();
	}

}

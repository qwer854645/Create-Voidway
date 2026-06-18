package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidTankOutputBlock extends AbstractVoidTankBlock<VoidTankOutputTileEntity> {

	public static final MapCodec<VoidTankOutputBlock> CODEC = simpleCodec(VoidTankOutputBlock::new);

	public VoidTankOutputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidTankOutputTileEntity> getBlockEntityClass() {
		return VoidTankOutputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidTankOutputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_TANK_OUTPUT.get();
	}

}

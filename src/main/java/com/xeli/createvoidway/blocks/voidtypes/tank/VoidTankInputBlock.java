package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidTankInputBlock extends AbstractVoidTankBlock<VoidTankInputTileEntity> {

	public static final MapCodec<VoidTankInputBlock> CODEC = simpleCodec(VoidTankInputBlock::new);

	public VoidTankInputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidTankInputTileEntity> getBlockEntityClass() {
		return VoidTankInputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidTankInputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_TANK_INPUT.get();
	}

}

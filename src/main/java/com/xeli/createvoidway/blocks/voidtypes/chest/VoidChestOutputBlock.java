package com.xeli.createvoidway.blocks.voidtypes.chest;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidChestOutputBlock extends AbstractVoidChestBlock<VoidChestOutputTileEntity> {

	public static final MapCodec<VoidChestOutputBlock> CODEC = simpleCodec(VoidChestOutputBlock::new);

	public VoidChestOutputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidChestOutputTileEntity> getBlockEntityClass() {
		return VoidChestOutputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidChestOutputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_CHEST_OUTPUT.get();
	}

}

package com.xeli.createvoidway.blocks.voidtypes.chest;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidChestInputBlock extends AbstractVoidChestBlock<VoidChestInputTileEntity> {

	public static final MapCodec<VoidChestInputBlock> CODEC = simpleCodec(VoidChestInputBlock::new);

	public VoidChestInputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidChestInputTileEntity> getBlockEntityClass() {
		return VoidChestInputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidChestInputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_CHEST_INPUT.get();
	}

}

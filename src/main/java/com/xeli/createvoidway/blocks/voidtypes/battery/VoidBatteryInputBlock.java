package com.xeli.createvoidway.blocks.voidtypes.battery;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidBatteryInputBlock extends AbstractVoidBatteryBlock<VoidBatteryInputTileEntity> {

	public static final MapCodec<VoidBatteryInputBlock> CODEC = simpleCodec(VoidBatteryInputBlock::new);

	public VoidBatteryInputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidBatteryInputTileEntity> getBlockEntityClass() {
		return VoidBatteryInputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidBatteryInputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_BATTERY_INPUT.get();
	}

}

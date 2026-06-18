package com.xeli.createvoidway.blocks.voidtypes.battery;

import com.mojang.serialization.MapCodec;
import com.xeli.createvoidway.blocks.RWTileEntities;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VoidBatteryOutputBlock extends AbstractVoidBatteryBlock<VoidBatteryOutputTileEntity> {

	public static final MapCodec<VoidBatteryOutputBlock> CODEC = simpleCodec(VoidBatteryOutputBlock::new);

	public VoidBatteryOutputBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codecImpl() {
		return CODEC;
	}

	@Override
	public Class<VoidBatteryOutputTileEntity> getBlockEntityClass() {
		return VoidBatteryOutputTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidBatteryOutputTileEntity> getBlockEntityType() {
		return RWTileEntities.VOID_BATTERY_OUTPUT.get();
	}

}

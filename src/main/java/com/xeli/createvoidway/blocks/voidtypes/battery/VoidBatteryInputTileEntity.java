package com.xeli.createvoidway.blocks.voidtypes.battery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VoidBatteryInputTileEntity extends AbstractVoidBatteryTileEntity {

	public VoidBatteryInputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public boolean isVoidBatteryInput() {
		return true;
	}

}

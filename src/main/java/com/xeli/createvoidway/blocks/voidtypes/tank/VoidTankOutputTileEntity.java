package com.xeli.createvoidway.blocks.voidtypes.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VoidTankOutputTileEntity extends AbstractVoidTankTileEntity {

	public VoidTankOutputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public boolean isVoidTankInput() {
		return false;
	}

}

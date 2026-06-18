package com.xeli.createvoidway.blocks.voidtypes.chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VoidChestInputTileEntity extends AbstractVoidChestTileEntity {

	public VoidChestInputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public boolean isVoidChestInput() {
		return true;
	}

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.createvoidway.void_chest_input");
	}

}

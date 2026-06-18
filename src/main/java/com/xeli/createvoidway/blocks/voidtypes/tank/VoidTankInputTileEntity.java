package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.xeli.createvoidway.blocks.voidtypes.VoidStorageLinkBehaviour;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;

public class VoidTankInputTileEntity extends AbstractVoidTankTileEntity {

	public VoidTankInputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidTankInputLinkSlot(index));
		link = new VoidStorageLinkBehaviour(this, slots);
	}

	@Override
	public boolean isVoidTankInput() {
		return true;
	}

}

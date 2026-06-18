package com.xeli.createvoidway.blocks;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class VoidShaftBuffers {

	private VoidShaftBuffers() {}

	public static SuperByteBuffer partialHalfFacing(Direction direction) {
		BlockState shaftState = AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, direction.getAxis());
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, shaftState, direction);
	}

}

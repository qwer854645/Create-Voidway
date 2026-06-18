package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Input tanks use a 180°-rotated model; slot interaction is rotated to match.
 */
public class VoidTankInputLinkSlot extends VoidLinkSlot {

	public VoidTankInputLinkSlot(int index) {
		super(index, state -> Direction.DOWN, VecHelper.voxelSpace(5.5F, 10.5F, -.001F));
	}

	@Override
	public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
		Vec3 offset = super.getLocalOffset(level, pos, state);
		return VecHelper.rotateCentered(offset, 180, Direction.Axis.Y);
	}

	@Override
	public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
		super.rotate(level, pos, state, ms);
		TransformStack.of(ms).rotateYDegrees(180);
	}

}

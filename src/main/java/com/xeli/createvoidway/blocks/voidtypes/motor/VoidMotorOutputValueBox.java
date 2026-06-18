package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class VoidMotorOutputValueBox extends ValueBoxTransform.Sided {

	@Override
	protected Vec3 getSouthLocation() {
		return VecHelper.voxelSpace(8, 8, 12.5);
	}

	@Override
	public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
		Direction facing = state.getValue(DirectionalKineticBlock.FACING);
		return super.getLocalOffset(level, pos, state).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(-1 / 16f));
	}

	@Override
	public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
		super.rotate(level, pos, state, ms);
		Direction facing = state.getValue(DirectionalKineticBlock.FACING);
		if (facing.getAxis() == Axis.Y)
			return;
		if (getSide() != Direction.UP)
			return;
		TransformStack.of(ms).rotateZDegrees(-AngleHelper.horizontalAngle(facing) + 180);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		Direction facing = state.getValue(DirectionalKineticBlock.FACING);
		if (facing.getAxis() != Axis.Y && direction == Direction.DOWN)
			return false;
		return direction.getAxis() != facing.getAxis();
	}

}

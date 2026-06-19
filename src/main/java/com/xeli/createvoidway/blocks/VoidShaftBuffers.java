package com.xeli.createvoidway.blocks;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class VoidShaftBuffers {

	public static final float VOXEL = 1f / 16f;
	public static final int HALF_SHAFT_LENGTH_VOXELS = 8;

	/** Voxels removed from the outward end of a bottom half-shaft (tank, teleport pad). */
	public static final int BOTTOM_STUB_SHORTEN_VOXELS = 3;

	/** Extra downward shift for void teleport pad / node terminal bottom shafts. */
	public static final int PAD_TERMINAL_SHAFT_DOWN_VOXELS = 1;

	/** Extra downward shift for void tank (input/output) bottom shafts. */
	public static final int TANK_SHAFT_DOWN_VOXELS = 2;

	/** Outward shift for void portal stress front/back shaft stubs. */
	public static final int PORTAL_STRESS_SHAFT_OUTWARD_VOXELS = 1;

	/** Bottom face plane for a DOWN-facing half-shaft in block space. */
	private static final float DOWN_HALF_BOTTOM_Y = 0f;

	private VoidShaftBuffers() {}

	public static SuperByteBuffer partialHalfFacing(Direction direction) {
		BlockState shaftState = AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, direction.getAxis());
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, shaftState, direction);
	}

	/**
	 * Bottom stub for void tank / teleport pad bottom frame cavities.
	 * Shortens the half-shaft from its outer end while keeping the bottom face flush at {@code y=0}.
	 */
	public static SuperByteBuffer bottomCavityStub() {
		return bottomCavityStub(0);
	}

	/**
	 * Same as {@link #bottomCavityStub()}, then lengthens the stub by {@code extraBottomVoxels} from the
	 * bottom face using the same bottom-pivot scale as {@link #bottomHalfAtFace(int)}.
	 */
	public static SuperByteBuffer bottomCavityStub(int extraBottomVoxels) {
		int stubLengthVoxels = HALF_SHAFT_LENGTH_VOXELS - BOTTOM_STUB_SHORTEN_VOXELS;
		SuperByteBuffer stub = shortenFromBottom(partialHalfFacing(Direction.DOWN), BOTTOM_STUB_SHORTEN_VOXELS);
		if (extraBottomVoxels <= 0)
			return stub;
		float yScale = (stubLengthVoxels + extraBottomVoxels) / (float) stubLengthVoxels;
		return pivotScale(stub, 0.5f, DOWN_HALF_BOTTOM_Y, 0.5f, 1f, yScale, 1f);
	}

	/**
	 * Remove {@code voxels} from the outer end of a DOWN half-shaft. The bottom attachment at {@code y=0}
	 * stays fixed; length is removed upward only.
	 */
	public static SuperByteBuffer shortenFromBottom(SuperByteBuffer shaft, int voxels) {
		if (voxels <= 0 || voxels >= HALF_SHAFT_LENGTH_VOXELS)
			return shaft;
		float sy = (HALF_SHAFT_LENGTH_VOXELS - voxels) / (float) HALF_SHAFT_LENGTH_VOXELS;
		return pivotScale(shaft, 0.5f, DOWN_HALF_BOTTOM_Y, 0.5f, 1f, sy, 1f);
	}

	/**
	 * Bottom half-shaft anchored to the block's lower face. {@code extraBottomVoxels} extends the stub
	 * downward beyond the face (in voxel units).
	 */
	public static SuperByteBuffer bottomHalfAtFace(int extraBottomVoxels) {
		SuperByteBuffer shaft = partialHalfFacing(Direction.DOWN);
		if (extraBottomVoxels <= 0)
			return shaft;
		float yScale = (HALF_SHAFT_LENGTH_VOXELS + extraBottomVoxels) / (float) HALF_SHAFT_LENGTH_VOXELS;
		return pivotScale(shaft, 0.5f, DOWN_HALF_BOTTOM_Y, 0.5f, 1f, yScale, 1f);
	}

	public static float flywheelBottomExtendedYOffset(int extraBottomVoxels) {
		return -extraBottomVoxels * VOXEL;
	}

	public static float padTerminalShaftDownOffset() {
		return -PAD_TERMINAL_SHAFT_DOWN_VOXELS * VOXEL;
	}

	public static float tankShaftDownOffset() {
		return -TANK_SHAFT_DOWN_VOXELS * VOXEL;
	}

	public static SuperByteBuffer translateOutward(SuperByteBuffer shaft, Direction outward, int voxels) {
		float offset = voxels * VOXEL;
		return shaft.translate(outward.getStepX() * offset, outward.getStepY() * offset, outward.getStepZ() * offset);
	}

	public static SuperByteBuffer pivotScale(SuperByteBuffer shaft, float px, float py, float pz,
			float sx, float sy, float sz) {
		return shaft
				.translate(-px, -py, -pz)
				.scale(sx, sy, sz)
				.translate(px, py, pz);
	}

}

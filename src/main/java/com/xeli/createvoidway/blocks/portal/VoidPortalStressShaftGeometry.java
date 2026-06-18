package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

final class VoidPortalStressShaftGeometry {

	private static final float STUB_SCALE = 0.15f;
	/** Center of a Create shaft-half model measured inward from the block face. */
	private static final float HALF_SHAFT_CENTER = 0.25f;
	/** Center of the scaled stub measured inward from the block face. */
	private static final float STUB_CENTER_INSET = STUB_SCALE * 0.25f;
	private static final float FACE_ALIGN_SHIFT = STUB_CENTER_INSET - HALF_SHAFT_CENTER;

	private VoidPortalStressShaftGeometry() {}

	static SuperByteBuffer createStub(Direction face) {
		SuperByteBuffer shaft = VoidShaftBuffers.partialHalfFacing(face);
		SuperByteBuffer scaled = switch (face) {
			case NORTH, SOUTH -> shaft.center().scale(1, 1, STUB_SCALE).uncenter();
			case WEST, EAST -> shaft.center().scale(STUB_SCALE, 1, 1).uncenter();
			default -> shaft.center().scale(1, STUB_SCALE, 1).uncenter();
		};
		return switch (face) {
			case NORTH -> scaled.translate(0, 0, FACE_ALIGN_SHIFT);
			case SOUTH -> scaled.translate(0, 0, -FACE_ALIGN_SHIFT);
			case WEST -> scaled.translate(FACE_ALIGN_SHIFT, 0, 0);
			case EAST -> scaled.translate(-FACE_ALIGN_SHIFT, 0, 0);
			case DOWN -> scaled.translate(0, FACE_ALIGN_SHIFT, 0);
			case UP -> scaled.translate(0, -FACE_ALIGN_SHIFT, 0);
		};
	}

	static Vector3f flywheelPosition(BlockPos visualPos, Direction face) {
		float centerX = visualPos.getX() + 0.5f;
		float centerY = visualPos.getY() + 0.5f;
		float centerZ = visualPos.getZ() + 0.5f;
		return switch (face) {
			case NORTH -> new Vector3f(centerX, centerY, visualPos.getZ() + STUB_CENTER_INSET);
			case SOUTH -> new Vector3f(centerX, centerY, visualPos.getZ() + 1f - STUB_CENTER_INSET);
			case WEST -> new Vector3f(visualPos.getX() + STUB_CENTER_INSET, centerY, centerZ);
			case EAST -> new Vector3f(visualPos.getX() + 1f - STUB_CENTER_INSET, centerY, centerZ);
			case DOWN -> new Vector3f(centerX, visualPos.getY() + STUB_CENTER_INSET, centerZ);
			case UP -> new Vector3f(centerX, visualPos.getY() + 1f - STUB_CENTER_INSET, centerZ);
		};
	}

}

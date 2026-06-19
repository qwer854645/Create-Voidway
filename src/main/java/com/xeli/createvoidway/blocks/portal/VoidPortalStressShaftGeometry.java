package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;

final class VoidPortalStressShaftGeometry {

	private static final float STUB_SCALE = 0.15f;

	private VoidPortalStressShaftGeometry() {}

	static SuperByteBuffer createStub(Direction face) {
		SuperByteBuffer shaft = VoidShaftBuffers.partialHalfFacing(face);
		shaft = scaleFromFace(shaft, face);
		return VoidShaftBuffers.translateOutward(shaft, face, VoidShaftBuffers.PORTAL_STRESS_SHAFT_OUTWARD_VOXELS);
	}

	/**
	 * Shorten the shaft stub from the block face inward while keeping the face-center attachment fixed.
	 * {@link VoidShaftBuffers#partialHalfFacing} already centers the half-shaft on the face; scaling around
	 * block center would pull the stub off that face and toward a block edge.
	 */
	private static SuperByteBuffer scaleFromFace(SuperByteBuffer shaft, Direction face) {
		return switch (face) {
			case NORTH -> pivotScale(shaft, 0.5f, 0.5f, 0f, 1f, 1f, STUB_SCALE);
			case SOUTH -> pivotScale(shaft, 0.5f, 0.5f, 1f, 1f, 1f, STUB_SCALE);
			case WEST -> pivotScale(shaft, 0f, 0.5f, 0.5f, STUB_SCALE, 1f, 1f);
			case EAST -> pivotScale(shaft, 1f, 0.5f, 0.5f, STUB_SCALE, 1f, 1f);
			case DOWN -> pivotScale(shaft, 0.5f, 0f, 0.5f, 1f, STUB_SCALE, 1f);
			case UP -> pivotScale(shaft, 0.5f, 1f, 0.5f, 1f, STUB_SCALE, 1f);
		};
	}

	private static SuperByteBuffer pivotScale(SuperByteBuffer shaft, float px, float py, float pz,
			float sx, float sy, float sz) {
		return VoidShaftBuffers.pivotScale(shaft, px, py, pz, sx, sy, sz);
	}

}

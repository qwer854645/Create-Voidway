package com.xeli.createvoidway.blocks.voidtypes;

import net.minecraft.core.Direction;

/**
 * Shared end-portal overlay settings for void devices. Overlays are drawn in block space without
 * blockstate rotation, so they must use {@link Direction#UP} to sit flat on the block top.
 */
public final class VoidPortalOverlay {

	public static final float CHEST_FRAME_WIDTH = 0.625F;
	public static final float CHEST_FRAME_Y = 0.626F;

	public static final float PAD_FRAME_WIDTH = 0.75F;

	/** Side observation windows on void fluid tanks (Create Utilities parity). */
	public static final float TANK_WINDOW_FRAME_WIDTH = 0.75F;
	public static final float TANK_WINDOW_FRAME_OFFSET = 0.124F;

	public static final float LINK_FRAME_WIDTH = 0.625F;
	public static final float LINK_FRAME_Y = 0.751F;

	private VoidPortalOverlay() {
	}

	public static boolean isUpFace(Direction direction) {
		return direction == Direction.UP;
	}

	public static float yOffset(Direction direction, float upOffset) {
		return direction.getAxis() == Direction.Axis.Y ? upOffset : 0.124F;
	}

}

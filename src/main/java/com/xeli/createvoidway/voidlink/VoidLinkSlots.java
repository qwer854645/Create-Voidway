package com.xeli.createvoidway.voidlink;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Function;

/**
 * Factory helpers for {@link VoidLinkSlot} placement.
 * <p>
 * {@link #southModelHorizontal} — slots baked on south (+Z); blockstates use
 * {@code north=180, south=0, west=90, east=270}. {@code slotFace} is the world face
 * the rotated model shows (typically {@code FACING}, toward the player).
 * <p>
 * Void motor blocks use north-baked models but the original Create Utilities
 * {@code FACING} + {@code z=-.001} slot setup (see {@code AbstractVoidMotorTileEntity}).
 */
public final class VoidLinkSlots {

	private VoidLinkSlots() {
	}

	public static Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> southModelHorizontal(
			Function<BlockState, Direction> slotFace, float yVoxels) {
		return VoidLinkSlot.makeSlots(index -> new VoidLinkSlot(index,
				state -> slotFace.apply(state).getOpposite(),
				VecHelper.voxelSpace(5.5F, yVoxels, -.001F)));
	}

}

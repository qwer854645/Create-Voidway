package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public final class VoidNodeNetworkIndex {

	private VoidNodeNetworkIndex() {
	}

	public record DimensionalBlockPos(ResourceLocation dimension, BlockPos pos) {
	}

	public static Set<DimensionalBlockPos> collectPositions(NetworkKey key) {
		Set<DimensionalBlockPos> positions = new LinkedHashSet<>();
		BiConsumer<ResourceLocation, BlockPos> collector = (dimension, pos) ->
				positions.add(new DimensionalBlockPos(dimension, pos));

		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.collectPositions(key, collector);
		return positions;
	}

}

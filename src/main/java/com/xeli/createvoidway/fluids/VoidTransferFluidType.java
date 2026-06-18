package com.xeli.createvoidway.fluids;

import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

/**
 * Server-side fluid type for void transfer fluid. Client rendering is registered in {@link VoidTransferFluidClientExtensions}.
 */
public final class VoidTransferFluidType {

	private VoidTransferFluidType() {}

	@SuppressWarnings("unused")
	public static FluidTypeFactory solidRendered(int fogColor, Supplier<Float> fogDistance) {
		return (properties, stillTexture, flowingTexture) -> new FluidType(properties);
	}

}

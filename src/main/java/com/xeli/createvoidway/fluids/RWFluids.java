package com.xeli.createvoidway.fluids;

import com.tterrag.registrate.util.entry.FluidEntry;
import com.xeli.createvoidway.RWTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.xeli.createvoidway.VoidwayMod.REGISTRATE;

public class RWFluids {

	/** Purple tone similar to Create's creative motor. */
	private static final int VOID_TRANSFER_FLUID_COLOR = 0x9D52FF;

	public static final FluidEntry<BaseFlowingFluid.Flowing> VOID_TRANSFER_FLUID = REGISTRATE
			.standardFluid("void_transfer_fluid",
					VoidTransferFluidType.solidRendered(VOID_TRANSFER_FLUID_COLOR, () -> 1f / 8f))
			.lang("Void Transfer Fluid")
			.tag(RWTags.VOID_TRANSFER_FLUID)
			.properties(b -> b.viscosity(2500)
					.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
					.tickRate(25)
					.slopeFindDistance(3)
					.explosionResistance(100f))
			.renderType(() -> RenderType::solid)
			.source(BaseFlowingFluid.Source::new)
			.block()
			.properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
			.build()
			.bucket()
			.build()
			.register();

	private RWFluids() {}

	public static void register() {}

	public static ItemStack getBucketStack() {
		return new ItemStack(REGISTRATE.get("void_transfer_fluid_bucket", Registries.ITEM).get());
	}

	public static boolean isVoidTransferFluid(Fluid fluid) {
		return fluid != null && fluid.is(RWTags.VOID_TRANSFER_FLUID);
	}

	/** Whether the stack may be inserted into a void motor input tank. */
	public static boolean isAllowedInVoidMotorInput(FluidStack stack) {
		return !stack.isEmpty() && stack.is(RWTags.VOID_TRANSFER_FLUID);
	}

	public static boolean isVoidTransferFluid(FluidStack stack) {
		return isAllowedInVoidMotorInput(stack);
	}

}

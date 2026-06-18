package com.xeli.createvoidway.blocks.voidtypes;

import com.xeli.createvoidway.VoidwayMod;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VoidStorageGoggleTooltip {

	private VoidStorageGoggleTooltip() {}

	public static void addRoleAndLink(List<Component> tooltip, String keyPrefix, boolean isInput, int linkedPartners) {
		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + (isInput ? "_input.role" : "_output.role"))
				.forGoggles(tooltip);

		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + (isInput ? "_input.linked_outputs" : "_output.linked_inputs"), linkedPartners)
				.forGoggles(tooltip);

		if (linkedPartners == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".not_linked")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		}
	}

	public static void addKineticStatus(List<Component> tooltip, String keyPrefix,
										int speedRpm, int channelStressDemand, int transferFluidDrain,
										boolean hasShaftConnection, boolean hasSource, boolean isOverStressed,
										boolean hasRequiredStress, boolean hasSufficientTransferFluid,
										boolean canOperate) {
		if (!hasShaftConnection) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".no_shaft")
					.forGoggles(tooltip);
			return;
		}

		if (speedRpm == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".needs_rotation")
					.forGoggles(tooltip);
			return;
		}

		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + ".speed", speedRpm)
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + ".channel_stress_demand", channelStressDemand)
				.forGoggles(tooltip);
		if (transferFluidDrain > 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".transfer_fluid_drain_per_tick", transferFluidDrain)
					.forGoggles(tooltip);
		}

		if (isOverStressed) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".over_stressed")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!hasSource) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".no_stress_source")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!hasSufficientTransferFluid) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".no_transfer_fluid")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (canOperate) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".operating")
					.style(ChatFormatting.GREEN)
					.forGoggles(tooltip);
		}
	}

}

package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.config.VoidwayConfig;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VoidMotorGoggleTooltip {

	private VoidMotorGoggleTooltip() {}

	public static void addInputRoleAndLink(List<Component> tooltip, int linkedPartners) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_input.role")
				.forGoggles(tooltip);

		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_input.linked_outputs", linkedPartners)
				.forGoggles(tooltip);

		if (linkedPartners == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.not_linked")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		}
	}

	public static void addChannelStressTotal(List<Component> tooltip, int channelStressTotal) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor.channel_stress_total", channelStressTotal)
				.forGoggles(tooltip);
	}

	public static void addInputInjectionStatus(List<Component> tooltip,
											   int speedRpm,
											   int receivedStress,
											   int localStressConsumption,
											   int channelStressContribution,
											   int transferFluidDrain,
											   boolean hasSource,
											   boolean isOverStressed,
											   boolean hasSufficientTransferFluid,
											   boolean canRelay) {
		if (speedRpm == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.needs_rotation")
					.forGoggles(tooltip);
			return;
		}

		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_input.speed", speedRpm)
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_input.received_stress", receivedStress)
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_input.local_stress_retained",
						VoidwayConfig.getInputLocalStressPercent(),
						localStressConsumption)
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_input.stress_contribution", channelStressContribution)
				.forGoggles(tooltip);
		if (transferFluidDrain > 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.transfer_fluid_drain_per_tick", transferFluidDrain)
					.forGoggles(tooltip);
		}

		if (isOverStressed) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.over_stressed")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!hasSource) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.no_stress_source")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!hasSufficientTransferFluid) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.no_transfer_fluid")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (canRelay) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_input.injecting")
					.style(ChatFormatting.GREEN)
					.forGoggles(tooltip);
		}
	}

}

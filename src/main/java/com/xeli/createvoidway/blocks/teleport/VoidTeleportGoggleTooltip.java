package com.xeli.createvoidway.blocks.teleport;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

public final class VoidTeleportGoggleTooltip {

	private VoidTeleportGoggleTooltip() {}

	public static void add(List<Component> tooltip, VoidTeleportPadTileEntity pad, boolean sneaking, FluidTank fluidTank) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_teleport_pad.role")
				.forGoggles(tooltip);

		if (!pad.hasBindingLink()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.not_bound")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.wrench_bind")
					.forGoggles(tooltip);
		} else if (!pad.hasConfiguredBindingLink()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.link_no_frequency")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.wrench_unbind")
					.forGoggles(tooltip);
		} else {
			switch (pad.getPairStatus()) {
				case VALID -> new LangBuilder(VoidwayMod.ID)
						.translate("void_teleport_pad.paired")
						.forGoggles(tooltip);
				case CONFLICT -> new LangBuilder(VoidwayMod.ID)
						.translate("void_teleport_pad.conflict", pad.getPadCount())
						.style(ChatFormatting.RED)
						.forGoggles(tooltip);
				default -> {
					if (pad.getPadCount() == 0) {
						new LangBuilder(VoidwayMod.ID)
								.translate("void_teleport_pad.unpaired")
								.style(ChatFormatting.RED)
								.forGoggles(tooltip);
					} else {
						new LangBuilder(VoidwayMod.ID)
								.translate("void_teleport_pad.waiting", pad.getPadCount())
								.forGoggles(tooltip);
					}
				}
			}
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.wrench_unbind")
					.forGoggles(tooltip);
		}

		pad.containedFluidTooltip(tooltip, sneaking, fluidTank);

		int pendingCount = pad.getPendingBatchEntityCount();
		int pendingCost = pendingCount > 0 ? pad.getPendingBatchFluidCost() : 0;
		if (pendingCount > 0) {
			ChatFormatting fluidColor = fluidTank.getFluidAmount() >= pendingCost
					? ChatFormatting.GRAY
					: ChatFormatting.RED;
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.estimated_fluid_cost", pendingCost)
					.style(fluidColor)
					.forGoggles(tooltip);
		}

		int speed = (int) Math.abs(pad.getTheoreticalSpeed());
		if (pad.getPairStatus() == VoidTeleportNetworkHandler.PairStatus.VALID && pad.getLinkDistance() > 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.link_distance", pad.getLinkDistance())
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.stress_demand", pad.getLinkStressDemand())
					.forGoggles(tooltip);
			if (VoidTeleportLinkMetrics.isStressCapped(pad.getLinkDistance())) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_teleport_pad.stress_capped", VoidwayConfig.getVoidTeleportStressMax())
						.style(ChatFormatting.GOLD)
						.forGoggles(tooltip);
			}
		}

		if (!pad.hasShaftConnection()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.no_shaft")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (speed == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.needs_rotation")
					.forGoggles(tooltip);
		} else if (pad.isOverStressed()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.over_stressed")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!pad.hasSource()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.no_stress_source")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (pendingCount > 0 && fluidTank.getFluidAmount() < pendingCost) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.insufficient_fluid_batch", pendingCost, pendingCount)
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (pad.canOperate()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_pad.ready")
					.style(ChatFormatting.GREEN)
					.forGoggles(tooltip);
			if (VoidwayConfig.isVoidTeleportFluidSharingEnabled()) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_teleport_pad.fluid_share", VoidwayConfig.getVoidTeleportFluidShareMbPerTick())
						.forGoggles(tooltip);
			}
			if (pad.getSyncedChargeTicks() > 0) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_teleport_pad.charging",
								pad.getSyncedChargeTicks(),
								VoidwayConfig.getVoidTeleportChargeTicks())
						.forGoggles(tooltip);
			}
		}
	}

}

package com.xeli.createvoidway.blocks.teleport;

import com.xeli.createvoidway.VoidwayMod;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VoidTeleportLinkGoggleTooltip {

	private VoidTeleportLinkGoggleTooltip() {}

	public static void add(List<Component> tooltip, VoidTeleportLinkTileEntity link) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_teleport_link.role")
				.forGoggles(tooltip);

		if (link.isMutuallyBound()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_link.bound")
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_link.wrench_unbind")
					.forGoggles(tooltip);
		} else {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_link.not_bound")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_link.wrench_bind")
					.forGoggles(tooltip);
		}

		if (!link.hasFrequencyConfigured()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_link.no_frequency")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_teleport_link.configured")
					.forGoggles(tooltip);
		}
	}

}

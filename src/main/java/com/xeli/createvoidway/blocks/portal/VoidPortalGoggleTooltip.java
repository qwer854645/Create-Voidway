package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkMetrics;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class VoidPortalGoggleTooltip {

	private VoidPortalGoggleTooltip() {}

	public static void addConnector(List<Component> tooltip, VoidPortalConnectorTileEntity portal) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_portal.connector.role")
				.forGoggles(tooltip);
		addPortalStatus(tooltip, portal.getLevel(), portal.getBlockPos(), portal);
	}

	public static void addFluid(List<Component> tooltip, VoidPortalFluidTileEntity fluid, boolean sneaking) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_portal.fluid.role")
				.forGoggles(tooltip);
		addFluidTank(tooltip, fluid.getFluidTank(), sneaking);
		addPortalStatus(tooltip, fluid.getLevel(), fluid.getBlockPos(), null);
	}

	public static void addStress(List<Component> tooltip, VoidPortalStressTileEntity stress) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_portal.stress.role")
				.forGoggles(tooltip);
		addStressKinetics(tooltip, stress);
		addPortalStatus(tooltip, stress.getLevel(), stress.getBlockPos(), null);
	}

	private static void addFluidTank(List<Component> tooltip, VoidTransferFluidTank tank, boolean sneaking) {
		int amount = tank.getFluidAmount();
		int capacity = tank.getCapacity();
		ChatFormatting color = amount > 0 ? ChatFormatting.GRAY : ChatFormatting.RED;
		new LangBuilder(VoidwayMod.ID)
				.translate("void_portal.fluid_stored", amount, capacity)
				.style(color)
				.forGoggles(tooltip);
		if (sneaking && !tank.getFluid().isEmpty()) {
			new LangBuilder(VoidwayMod.ID)
					.text(tank.getFluid().getHoverName().getString())
					.style(ChatFormatting.DARK_GRAY)
					.forGoggles(tooltip);
		}
	}

	private static void addStressKinetics(List<Component> tooltip, VoidPortalStressTileEntity stress) {
		int speed = (int) Math.abs(stress.getTheoreticalSpeed());
		if (!stress.hasShaftConnection()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.no_shaft")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			return;
		}
		if (speed == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.needs_rotation")
					.forGoggles(tooltip);
			return;
		}
		new LangBuilder(VoidwayMod.ID)
				.translate("void_portal.speed", speed)
				.forGoggles(tooltip);
		if (stress.isOverStressed()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.over_stressed")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!stress.hasSource()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.no_stress_source")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (stress.hasRequiredStress()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.stress_ok")
					.style(ChatFormatting.GREEN)
					.forGoggles(tooltip);
		}
	}

	private static void addPortalStatus(List<Component> tooltip, @Nullable Level level, BlockPos pos,
			@Nullable VoidPortalConnectorTileEntity directConnector) {
		VoidPortalConnectorTileEntity connector = directConnector != null
				? directConnector
				: findConnector(level, pos);
		if (connector == null) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.not_in_frame")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			return;
		}

		VoidPortalShape shape = connector.getActiveShape();
		if (shape == null) {
			int[] nonSquare = VoidPortalShape.getNonSquareOpeningDimensions(level, pos);
			if (nonSquare != null) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_portal.not_square")
						.style(ChatFormatting.RED)
						.forGoggles(tooltip);
				new LangBuilder(VoidwayMod.ID)
						.translate("void_portal.opening_size", nonSquare[0], nonSquare[1])
						.style(ChatFormatting.GRAY)
						.forGoggles(tooltip);
				return;
			}
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.invalid_frame")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			return;
		}

		new LangBuilder(VoidwayMod.ID)
				.translate("void_portal.valid_frame", shape.innerWidth(), shape.innerHeight())
				.forGoggles(tooltip);

		if (!connector.hasFrequencyConfigured()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.no_frequency")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			return;
		}

		switch (connector.getPairStatus()) {
			case VALID -> new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.paired")
					.forGoggles(tooltip);
			case CONFLICT -> new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.conflict")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
			default -> new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.unpaired")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		}

		if (connector.getPairStatus() == VoidPortalNetworkHandler.PairStatus.VALID && connector.getLinkDistance() > 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.link_distance", connector.getLinkDistance())
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.stress_demand", connector.getLinkStressDemand())
					.forGoggles(tooltip);
			if (VoidTeleportLinkMetrics.isStressCapped(connector.getLinkDistance())) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_portal.stress_capped", VoidwayConfig.getVoidTeleportStressMax())
						.style(ChatFormatting.GOLD)
						.forGoggles(tooltip);
			}
		}

		if (!connector.hasRequiredStress()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.insufficient_stress")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!connector.hasTransferFluid()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.no_transfer_fluid")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (connector.shouldActivatePortalBlocks()) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.portal_active")
					.style(ChatFormatting.GREEN)
					.forGoggles(tooltip);
		} else {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_portal.portal_inactive")
					.forGoggles(tooltip);
		}
	}

	@Nullable
	private static VoidPortalConnectorTileEntity findConnector(@Nullable Level level, BlockPos pos) {
		if (level == null)
			return null;
		VoidPortalShape shape = VoidPortalShape.findAt(level, pos);
		if (shape == null)
			return null;
		if (level.getBlockEntity(shape.connectorPos()) instanceof VoidPortalConnectorTileEntity connector)
			return connector;
		return null;
	}

}

package com.xeli.createvoidway;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.blocks.RWTileEntities;
import com.xeli.createvoidway.blocks.voidtypes.RWContainerTypes;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBattery;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryData;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInventory;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInventoriesData;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageNetworkHandler;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportNetworkHandler;
import com.xeli.createvoidway.blocks.portal.VoidPortalNetworkHandler;
import com.xeli.createvoidway.blocks.portal.VoidPortalTrackProvider;
import com.xeli.createvoidway.blocks.terminal.VoidNodeNamesData;
import com.xeli.createvoidway.blocks.terminal.VoidTerminalNetworkHandler;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTank;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTanksData;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.config.VoidwayStress;
import com.xeli.createvoidway.events.ClientEvents;
import com.xeli.createvoidway.events.CommonEvents;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.mountedstorage.RWMountedStorages;
import com.xeli.createvoidway.networking.RWPackets;
import com.xeli.createvoidway.tabs.RWCreativeTabs;
import com.xeli.createvoidway.voidlink.VoidLinkHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

@Mod(VoidwayMod.ID)
public class VoidwayMod {

	public static final String ID = "createvoidway";
	public static final String NAME = "Create: Voidway";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	public static final VoidMotorNetworkHandler VOID_MOTOR_LINK_NETWORK_HANDLER = new VoidMotorNetworkHandler();
	public static final VoidStorageNetworkHandler VOID_STORAGE_LINK_NETWORK_HANDLER = new VoidStorageNetworkHandler();
	public static final VoidTeleportNetworkHandler VOID_TELEPORT_NETWORK_HANDLER = new VoidTeleportNetworkHandler();
	public static final VoidPortalNetworkHandler VOID_PORTAL_NETWORK_HANDLER = new VoidPortalNetworkHandler();
	public static final VoidTerminalNetworkHandler VOID_TERMINAL_NETWORK_HANDLER = new VoidTerminalNetworkHandler();
	public static VoidChestInventoriesData VOID_CHEST_INVENTORIES_DATA;

	public static VoidTanksData VOID_TANKS_DATA;
	public static VoidBatteryData VOID_BATTERIES_DATA;
	public static VoidNodeNamesData VOID_NODE_NAMES_DATA;

	public VoidwayMod(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, VoidwayConfig.SPEC);

		modEventBus.addListener(VoidwayMod::onBuildCreativeModeTabContents);
		REGISTRATE.registerEventListeners(modEventBus);

		RWFluids.register();
		RWBlocks.register();
		RWItems.register();
		RWTileEntities.register();
		RWContainerTypes.register();
		RWCreativeTabs.register(modEventBus);
		RWMountedStorages.register();

		modEventBus.addListener(VoidwayMod::commonSetup);
		modEventBus.addListener(VoidwayMod::registerCapabilities);
		modEventBus.addListener(RWPackets::register);

		NeoForge.EVENT_BUS.addListener(CommonEvents::onLoad);
		NeoForge.EVENT_BUS.addListener(CommonEvents::onUnload);
		NeoForge.EVENT_BUS.addListener(VoidLinkHandler::onBlockActivated);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			VoidwayClient.onCtorClient(modEventBus);
			NeoForge.EVENT_BUS.addListener(ClientEvents::onTick);
		}
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			VoidwayStress.register();
			VoidPortalTrackProvider.register();
		});
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RWTileEntities.VOID_CHEST_INPUT.get(),
				(blockEntity, side) -> blockEntity.getAutomationHandler());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RWTileEntities.VOID_CHEST_OUTPUT.get(),
				(blockEntity, side) -> blockEntity.getAutomationHandler());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_TANK_INPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_TANK_OUTPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_CHEST_INPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_CHEST_OUTPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_MOTOR_INPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_BATTERY_INPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_BATTERY_OUTPUT.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, RWTileEntities.VOID_BATTERY_INPUT.get(),
				(blockEntity, side) -> blockEntity.getEnergyHandler());
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, RWTileEntities.VOID_BATTERY_OUTPUT.get(),
				(blockEntity, side) -> blockEntity.getEnergyHandler());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_TELEPORT_PAD.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RWTileEntities.VOID_TELEPORT_PAD.get(),
				(blockEntity, side) -> blockEntity.getItemHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_PORTAL_FLUID.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RWTileEntities.VOID_NODE_TERMINAL.get(),
				(blockEntity, side) -> blockEntity.getFluidHandler(side));
	}

	private static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		disableRegistrateCreativeTabPopulation();
	}

	private static void disableRegistrateCreativeTabPopulation() {
		REGISTRATE.setCreativeTab(null);
		try {
			Field field = com.tterrag.registrate.AbstractRegistrate.class.getDeclaredField("creativeModeTabModifiers");
			field.setAccessible(true);
			Object value = field.get(REGISTRATE);
			if (value instanceof com.google.common.collect.Multimap<?, ?> creativeModeTabModifiers) {
				creativeModeTabModifiers.clear();
			}
		} catch (ReflectiveOperationException e) {
			LOGGER.warn("Failed to disable Registrate creative tab population", e);
		}
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}

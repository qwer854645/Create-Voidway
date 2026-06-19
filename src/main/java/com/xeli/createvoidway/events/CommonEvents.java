package com.xeli.createvoidway.events;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryData;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInventoriesData;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTanksData;
import com.xeli.createvoidway.blocks.terminal.VoidNodeNamesData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.event.level.LevelEvent;

public class CommonEvents {

	public static void onLoad(LevelEvent.Load event) {
		LevelAccessor level = event.getLevel();
		VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.onLoadWorld(level);

		MinecraftServer server = level.getServer();
		if (server == null)
			return;

		DimensionDataStorage dataStorage = server.overworld().getDataStorage();

		VoidwayMod.VOID_CHEST_INVENTORIES_DATA = dataStorage
				.computeIfAbsent(new SavedData.Factory<>(VoidChestInventoriesData::new, VoidChestInventoriesData::load), "VoidChestInventories");

		VoidwayMod.VOID_TANKS_DATA = dataStorage
				.computeIfAbsent(new SavedData.Factory<>(VoidTanksData::new, VoidTanksData::load), "VoidTanks");

		VoidwayMod.VOID_BATTERIES_DATA = dataStorage
				.computeIfAbsent(new SavedData.Factory<>(VoidBatteryData::new, VoidBatteryData::load), "VoidBatteries");

		VoidwayMod.VOID_NODE_NAMES_DATA = dataStorage
				.computeIfAbsent(new SavedData.Factory<>(VoidNodeNamesData::new, VoidNodeNamesData::load), "VoidNodeNames");
	}

	public static void onUnload(LevelEvent.Unload event) {
		VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
	}

}

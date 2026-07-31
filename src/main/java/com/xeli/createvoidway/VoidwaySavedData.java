package com.xeli.createvoidway;

import com.xeli.createvoidway.blocks.terminal.VoidNodeNamesData;
import com.xeli.createvoidway.blocks.terminal.VoidTerminalNetworkData;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryData;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInventoriesData;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTanksData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Lazily binds overworld SavedData into {@link VoidwayMod} statics so early block-entity
 * init never sees null after the server exists.
 */
public final class VoidwaySavedData {

	private VoidwaySavedData() {
	}

	public static void ensureLoaded(LevelAccessor level) {
		if (level == null)
			return;
		MinecraftServer server = level.getServer();
		if (server == null && level instanceof ServerLevel serverLevel)
			server = serverLevel.getServer();
		ensureLoaded(server);
	}

	public static void ensureLoaded(MinecraftServer server) {
		if (server == null)
			return;
		DimensionDataStorage dataStorage = server.overworld().getDataStorage();

		if (VoidwayMod.VOID_CHEST_INVENTORIES_DATA == null)
			VoidwayMod.VOID_CHEST_INVENTORIES_DATA = dataStorage.computeIfAbsent(
					new SavedData.Factory<>(VoidChestInventoriesData::new, VoidChestInventoriesData::load),
					"VoidChestInventories");

		if (VoidwayMod.VOID_TANKS_DATA == null)
			VoidwayMod.VOID_TANKS_DATA = dataStorage.computeIfAbsent(
					new SavedData.Factory<>(VoidTanksData::new, VoidTanksData::load), "VoidTanks");

		if (VoidwayMod.VOID_BATTERIES_DATA == null)
			VoidwayMod.VOID_BATTERIES_DATA = dataStorage.computeIfAbsent(
					new SavedData.Factory<>(VoidBatteryData::new, VoidBatteryData::load), "VoidBatteries");

		if (VoidwayMod.VOID_NODE_NAMES_DATA == null)
			VoidwayMod.VOID_NODE_NAMES_DATA = dataStorage.computeIfAbsent(
					new SavedData.Factory<>(VoidNodeNamesData::new, VoidNodeNamesData::load), "VoidNodeNames");

		if (VoidwayMod.VOID_TERMINAL_NETWORK_DATA == null)
			VoidwayMod.VOID_TERMINAL_NETWORK_DATA = dataStorage.computeIfAbsent(
					new SavedData.Factory<>(VoidTerminalNetworkData::new, VoidTerminalNetworkData::load),
					"VoidTerminalNetwork");
	}

	public static void clearStatics() {
		VoidwayMod.VOID_CHEST_INVENTORIES_DATA = null;
		VoidwayMod.VOID_TANKS_DATA = null;
		VoidwayMod.VOID_BATTERIES_DATA = null;
		VoidwayMod.VOID_NODE_NAMES_DATA = null;
		VoidwayMod.VOID_TERMINAL_NETWORK_DATA = null;
	}

}

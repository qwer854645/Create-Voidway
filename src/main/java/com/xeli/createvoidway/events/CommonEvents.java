package com.xeli.createvoidway.events;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.VoidwaySavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class CommonEvents {

	public static void onLoad(LevelEvent.Load event) {
		LevelAccessor level = event.getLevel();
		if (level.isClientSide())
			return;

		VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onLoadWorld(level);
		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.onLoadWorld(level);

		MinecraftServer server = level.getServer();
		if (server == null)
			return;

		VoidwaySavedData.ensureLoaded(server);
	}

	public static void onUnload(LevelEvent.Unload event) {
		if (event.getLevel().isClientSide())
			return;

		// Dimension unload must NOT wipe frequency indexes — cross-dim partners stay
		// registered until destroyed or the server stops. Only clear cooldown sweeps.
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
	}

	public static void onServerStopping(ServerStoppingEvent event) {
		VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER.clearAll();
		VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.clearAll();
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.clearAll();
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.clearAll();
		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.clearAll();
		VoidwaySavedData.clearStatics();
	}

}

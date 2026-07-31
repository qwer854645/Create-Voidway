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
		VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
	}

	public static void onServerStopping(ServerStoppingEvent event) {
		VoidwaySavedData.clearStatics();
	}

}

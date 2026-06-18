package com.xeli.createvoidway.events;

import com.xeli.createvoidway.voidlink.VoidLinkRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class ClientEvents {

	public static void onTick(ClientTickEvent.Post event) {
		if (!isGameActive())
			return;
		VoidLinkRenderer.tick();
	}

	protected static boolean isGameActive() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level != null && mc.player != null && mc.isRunning();
	}

}

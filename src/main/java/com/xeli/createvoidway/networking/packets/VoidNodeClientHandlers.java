package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalScreen;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class VoidNodeClientHandlers {

	private VoidNodeClientHandlers() {
	}

	static void onNodeList(VoidNodeListPacket packet) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.containerMenu instanceof VoidNodeTerminalContainer menu
				&& menu.matchesTerminal(packet.terminalPos())) {
			menu.updateNodes(packet.nodes());
			VoidNodeListPacket.clearPending(packet.terminalPos());
			if (mc.screen instanceof VoidNodeTerminalScreen screen && screen.getMenu() == menu)
				screen.onNodesUpdated();
			return;
		}
		VoidNodeListPacket.storePending(packet);
	}

	static void onPlayerList(VoidNodePlayerListPacket packet) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.containerMenu instanceof VoidNodeTerminalContainer menu
				&& menu.matchesTerminal(packet.terminalPos())) {
			menu.updatePlayers(packet.hasDeathLocation(), packet.deathDistanceBlocks(), packet.players());
			VoidNodePlayerListPacket.clearPending(packet.terminalPos());
			if (mc.screen instanceof VoidNodeTerminalScreen screen && screen.getMenu() == menu)
				screen.onPlayersUpdated();
			return;
		}
		VoidNodePlayerListPacket.storePending(packet);
	}

	static void onPortableList(PortableVoidTerminalListPacket packet) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.containerMenu instanceof PortableVoidTerminalContainer menu
				&& menu.matchesBinding(packet.hand(), packet.networkKey())) {
			menu.updateNodes(packet.nodes());
			PortableVoidTerminalListPacket.clearPending(packet);
			if (mc.screen instanceof PortableVoidTerminalScreen screen && screen.getMenu() == menu)
				screen.onNodesUpdated();
			return;
		}
		PortableVoidTerminalListPacket.storePending(packet);
	}

}

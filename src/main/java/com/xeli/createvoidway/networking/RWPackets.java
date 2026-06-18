package com.xeli.createvoidway.networking;

import com.xeli.createvoidway.networking.packets.VoidBatteryUpdatePacket;
import com.xeli.createvoidway.networking.packets.VoidChestUpdatePacket;
import com.xeli.createvoidway.networking.packets.VoidTankUpdatePacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RWPackets {

	public static final String NETWORK_VERSION = "3";

	private RWPackets() {}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
		registrar.playToClient(VoidTankUpdatePacket.TYPE, VoidTankUpdatePacket.STREAM_CODEC, VoidTankUpdatePacket::handle);
		registrar.playToClient(VoidBatteryUpdatePacket.TYPE, VoidBatteryUpdatePacket.STREAM_CODEC, VoidBatteryUpdatePacket::handle);
		registrar.playToClient(VoidChestUpdatePacket.TYPE, VoidChestUpdatePacket.STREAM_CODEC, VoidChestUpdatePacket::handle);
	}
}

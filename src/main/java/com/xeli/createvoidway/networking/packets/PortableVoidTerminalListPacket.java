package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.VoidNodeEntry;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.networking.RWStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PortableVoidTerminalListPacket(InteractionHand hand, NetworkKey networkKey, List<VoidNodeEntry> nodes)
		implements CustomPacketPayload {

	public record ListKey(InteractionHand hand, NetworkKey networkKey) {
	}

	private static final Map<ListKey, List<VoidNodeEntry>> PENDING = new HashMap<>();

	public static final Type<PortableVoidTerminalListPacket> TYPE = new Type<>(
			VoidwayMod.asResource("portable_void_terminal_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PortableVoidTerminalListPacket> STREAM_CODEC = StreamCodec.composite(
			RWStreamCodecs.INTERACTION_HAND, PortableVoidTerminalListPacket::hand,
			RWStreamCodecs.NETWORK_KEY, PortableVoidTerminalListPacket::networkKey,
			VoidNodeEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), PortableVoidTerminalListPacket::nodes,
			PortableVoidTerminalListPacket::new);

	public static void handle(PortableVoidTerminalListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!FMLEnvironment.dist.isClient())
				return;
			VoidNodeClientHandlers.onPortableList(packet);
		});
	}

	static void storePending(PortableVoidTerminalListPacket packet) {
		PENDING.put(new ListKey(packet.hand(), packet.networkKey()), packet.nodes());
	}

	static void clearPending(PortableVoidTerminalListPacket packet) {
		PENDING.remove(new ListKey(packet.hand(), packet.networkKey()));
	}

	public static void applyPending(PortableVoidTerminalContainer menu) {
		List<VoidNodeEntry> pending = PENDING.remove(new ListKey(menu.getHand(), menu.getNetworkKey()));
		if (pending != null)
			menu.updateNodes(pending);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeEntry;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record VoidNodeListPacket(BlockPos terminalPos, List<VoidNodeEntry> nodes) implements CustomPacketPayload {

	private static final Map<BlockPos, List<VoidNodeEntry>> PENDING = new HashMap<>();

	public static final Type<VoidNodeListPacket> TYPE = new Type<>(VoidwayMod.asResource("void_node_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeListPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeListPacket::terminalPos,
			VoidNodeEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), VoidNodeListPacket::nodes,
			VoidNodeListPacket::new);

	public static void handle(VoidNodeListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() == null)
				return;
			if (context.player().containerMenu instanceof VoidNodeTerminalContainer menu
					&& menu.matchesTerminal(packet.terminalPos())) {
				menu.updateNodes(packet.nodes());
				PENDING.remove(packet.terminalPos());
				if (Minecraft.getInstance().screen instanceof VoidNodeTerminalScreen screen
						&& screen.getMenu() == menu)
					screen.onNodesUpdated();
				return;
			}
			PENDING.put(packet.terminalPos(), packet.nodes());
		});
	}

	public static void applyPending(VoidNodeTerminalContainer menu) {
		List<VoidNodeEntry> pending = PENDING.remove(menu.getTerminalPos());
		if (pending != null)
			menu.updateNodes(pending);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

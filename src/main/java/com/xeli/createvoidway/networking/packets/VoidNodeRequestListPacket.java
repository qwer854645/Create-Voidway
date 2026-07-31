package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VoidNodeRequestListPacket(BlockPos terminalPos) implements CustomPacketPayload {

	public static final Type<VoidNodeRequestListPacket> TYPE = new Type<>(VoidwayMod.asResource("void_node_request_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeRequestListPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeRequestListPacket::terminalPos,
			VoidNodeRequestListPacket::new);

	public static void handle(VoidNodeRequestListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player))
				return;
			VoidNodeTerminalTileEntity terminal = VoidNodeService.resolveAuthorizedTerminal(player,
					packet.terminalPos());
			if (terminal == null)
				return;
			// Listing must not require continuous operate/cooldown checks — fluid drains every tick
			// while the GUI is open, which previously aborted refreshes and left an empty node list.
			VoidNodeService.sendNodeList(player, terminal);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VoidNodeRequestPlayerListPacket(BlockPos terminalPos) implements CustomPacketPayload {

	public static final Type<VoidNodeRequestPlayerListPacket> TYPE = new Type<>(
			VoidwayMod.asResource("void_node_request_player_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeRequestPlayerListPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeRequestPlayerListPacket::terminalPos,
			VoidNodeRequestPlayerListPacket::new);

	public static void handle(VoidNodeRequestPlayerListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player))
				return;
			VoidNodeTerminalTileEntity terminal = VoidNodeService.resolveTerminal(player.serverLevel(),
					packet.terminalPos());
			if (terminal == null)
				return;
			if (!terminal.canOperate()) {
				player.displayClientMessage(
						Component.translatable("createvoidway.portable_void_terminal.terminal_unavailable"), true);
				return;
			}
			VoidNodeService.sendPlayerList(player, terminal);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

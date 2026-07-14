package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VoidNodeTeleportDeathPacket(BlockPos terminalPos) implements CustomPacketPayload {

	public static final Type<VoidNodeTeleportDeathPacket> TYPE = new Type<>(VoidwayMod.asResource("void_node_teleport_death"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeTeleportDeathPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeTeleportDeathPacket::terminalPos,
			VoidNodeTeleportDeathPacket::new);

	public static void handle(VoidNodeTeleportDeathPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player)
				VoidNodeService.teleportToDeath(player, packet.terminalPos());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record VoidNodeTeleportPlayerPacket(BlockPos terminalPos, UUID targetPlayerUuid) implements CustomPacketPayload {

	public static final Type<VoidNodeTeleportPlayerPacket> TYPE = new Type<>(
			VoidwayMod.asResource("void_node_teleport_player"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeTeleportPlayerPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeTeleportPlayerPacket::terminalPos,
			UUIDUtil.STREAM_CODEC, VoidNodeTeleportPlayerPacket::targetPlayerUuid,
			VoidNodeTeleportPlayerPacket::new);

	public static void handle(VoidNodeTeleportPlayerPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player)
				VoidNodeService.teleportToPlayer(player, packet.terminalPos(), packet.targetPlayerUuid());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

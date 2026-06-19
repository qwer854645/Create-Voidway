package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VoidNodeTeleportPacket(BlockPos terminalPos, ResourceLocation targetDimension,
		BlockPos targetPos) implements CustomPacketPayload {

	public static final Type<VoidNodeTeleportPacket> TYPE = new Type<>(VoidwayMod.asResource("void_node_teleport"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeTeleportPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeTeleportPacket::terminalPos,
			ResourceLocation.STREAM_CODEC, VoidNodeTeleportPacket::targetDimension,
			BlockPos.STREAM_CODEC, VoidNodeTeleportPacket::targetPos,
			VoidNodeTeleportPacket::new);

	public static void handle(VoidNodeTeleportPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player)
				VoidNodeService.teleportPlayer(player, packet.terminalPos(), packet.targetDimension(),
						packet.targetPos());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

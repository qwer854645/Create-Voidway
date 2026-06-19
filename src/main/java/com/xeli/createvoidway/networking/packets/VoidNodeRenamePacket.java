package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VoidNodeRenamePacket(BlockPos terminalPos, ResourceLocation targetDimension, BlockPos targetPos,
		String newName) implements CustomPacketPayload {

	public static final Type<VoidNodeRenamePacket> TYPE = new Type<>(VoidwayMod.asResource("void_node_rename"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeRenamePacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodeRenamePacket::terminalPos,
			ResourceLocation.STREAM_CODEC, VoidNodeRenamePacket::targetDimension,
			BlockPos.STREAM_CODEC, VoidNodeRenamePacket::targetPos,
			ByteBufCodecs.STRING_UTF8, VoidNodeRenamePacket::newName,
			VoidNodeRenamePacket::new);

	public static void handle(VoidNodeRenamePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player)
				VoidNodeService.renameNode(player, packet.terminalPos(), packet.targetDimension(),
						packet.targetPos(), packet.newName());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

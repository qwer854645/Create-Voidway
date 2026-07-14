package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.VoidTerminalPlayerEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record VoidNodePlayerListPacket(BlockPos terminalPos, boolean hasDeathLocation, int deathDistanceBlocks,
		List<VoidTerminalPlayerEntry> players) implements CustomPacketPayload {

	private static final Map<BlockPos, VoidNodePlayerListPacket> PENDING = new HashMap<>();

	public static final Type<VoidNodePlayerListPacket> TYPE = new Type<>(VoidwayMod.asResource("void_node_player_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodePlayerListPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, VoidNodePlayerListPacket::terminalPos,
			ByteBufCodecs.BOOL, VoidNodePlayerListPacket::hasDeathLocation,
			ByteBufCodecs.VAR_INT, VoidNodePlayerListPacket::deathDistanceBlocks,
			VoidTerminalPlayerEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), VoidNodePlayerListPacket::players,
			VoidNodePlayerListPacket::new);

	public static void handle(VoidNodePlayerListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!FMLEnvironment.dist.isClient())
				return;
			VoidNodeClientHandlers.onPlayerList(packet);
		});
	}

	static void storePending(VoidNodePlayerListPacket packet) {
		PENDING.put(packet.terminalPos(), packet);
	}

	static void clearPending(BlockPos terminalPos) {
		PENDING.remove(terminalPos);
	}

	public static void applyPending(VoidNodeTerminalContainer menu) {
		VoidNodePlayerListPacket pending = PENDING.remove(menu.getTerminalPos());
		if (pending != null)
			menu.updatePlayers(pending.hasDeathLocation(), pending.deathDistanceBlocks(), pending.players());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}

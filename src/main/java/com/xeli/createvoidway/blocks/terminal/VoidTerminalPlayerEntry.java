package com.xeli.createvoidway.blocks.terminal;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record VoidTerminalPlayerEntry(UUID uuid, String displayName, ResourceLocation dimension,
		int distanceBlocks) {

	public static final int DISTANCE_OTHER_DIMENSION = -1;

	public static final StreamCodec<RegistryFriendlyByteBuf, VoidTerminalPlayerEntry> STREAM_CODEC = StreamCodec.of(
			VoidTerminalPlayerEntry::write, VoidTerminalPlayerEntry::read);

	private static void write(RegistryFriendlyByteBuf buf, VoidTerminalPlayerEntry entry) {
		UUIDUtil.STREAM_CODEC.encode(buf, entry.uuid());
		ByteBufCodecs.STRING_UTF8.encode(buf, entry.displayName());
		ResourceLocation.STREAM_CODEC.encode(buf, entry.dimension());
		ByteBufCodecs.VAR_INT.encode(buf, entry.distanceBlocks());
	}

	private static VoidTerminalPlayerEntry read(RegistryFriendlyByteBuf buf) {
		return new VoidTerminalPlayerEntry(
				UUIDUtil.STREAM_CODEC.decode(buf),
				ByteBufCodecs.STRING_UTF8.decode(buf),
				ResourceLocation.STREAM_CODEC.decode(buf),
				ByteBufCodecs.VAR_INT.decode(buf));
	}

	public Component distanceText() {
		if (distanceBlocks == DISTANCE_OTHER_DIMENSION)
			return Component.translatable(VoidNodeDiscovery.dimensionTranslationKey(dimension));
		return Component.translatable("createvoidway.void_node_terminal.distance.blocks", distanceBlocks);
	}

}

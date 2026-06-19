package com.xeli.createvoidway.blocks.terminal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record VoidNodeEntry(ResourceLocation dimension, BlockPos pos, String typeId, String displayName,
		String renameName, boolean currentTerminal, int distanceBlocks) {

	public static final int DISTANCE_OTHER_DIMENSION = -1;

	public static final StreamCodec<RegistryFriendlyByteBuf, VoidNodeEntry> STREAM_CODEC = StreamCodec.of(
			VoidNodeEntry::write, VoidNodeEntry::read);

	private static void write(RegistryFriendlyByteBuf buf, VoidNodeEntry entry) {
		ResourceLocation.STREAM_CODEC.encode(buf, entry.dimension());
		BlockPos.STREAM_CODEC.encode(buf, entry.pos());
		ByteBufCodecs.STRING_UTF8.encode(buf, entry.typeId());
		ByteBufCodecs.STRING_UTF8.encode(buf, entry.displayName());
		ByteBufCodecs.STRING_UTF8.encode(buf, entry.renameName());
		ByteBufCodecs.BOOL.encode(buf, entry.currentTerminal());
		ByteBufCodecs.VAR_INT.encode(buf, entry.distanceBlocks());
	}

	private static VoidNodeEntry read(RegistryFriendlyByteBuf buf) {
		return new VoidNodeEntry(
				ResourceLocation.STREAM_CODEC.decode(buf),
				BlockPos.STREAM_CODEC.decode(buf),
				ByteBufCodecs.STRING_UTF8.decode(buf),
				ByteBufCodecs.STRING_UTF8.decode(buf),
				ByteBufCodecs.STRING_UTF8.decode(buf),
				ByteBufCodecs.BOOL.decode(buf),
				ByteBufCodecs.VAR_INT.decode(buf));
	}

	public Component distanceText() {
		if (distanceBlocks == DISTANCE_OTHER_DIMENSION)
			return Component.translatable(VoidNodeDiscovery.dimensionTranslationKey(dimension));
		return Component.translatable("createvoidway.void_node_terminal.distance.blocks", distanceBlocks);
	}

}

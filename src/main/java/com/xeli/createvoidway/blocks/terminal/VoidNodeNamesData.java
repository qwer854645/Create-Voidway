package com.xeli.createvoidway.blocks.terminal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class VoidNodeNamesData extends SavedData {

	private final Map<String, String> names = new HashMap<>();

	public static String encode(ResourceLocation dimension, BlockPos pos) {
		return dimension + "|" + pos.asLong();
	}

	public static BlockPos decodePos(String key) {
		int sep = key.lastIndexOf('|');
		if (sep < 0)
			return BlockPos.ZERO;
		return BlockPos.of(Long.parseLong(key.substring(sep + 1)));
	}

	public static ResourceLocation decodeDimension(String key) {
		int sep = key.lastIndexOf('|');
		if (sep < 0)
			return ResourceLocation.withDefaultNamespace("overworld");
		return ResourceLocation.parse(key.substring(0, sep));
	}

	public String getName(ResourceLocation dimension, BlockPos pos) {
		return names.get(encode(dimension, pos));
	}

	public void setName(ResourceLocation dimension, BlockPos pos, String name) {
		String key = encode(dimension, pos);
		if (name == null || name.isBlank()) {
			if (names.remove(key) != null)
				setDirty();
			return;
		}
		String trimmed = name.trim();
		if (trimmed.length() > 32)
			trimmed = trimmed.substring(0, 32);
		if (!trimmed.equals(names.get(key))) {
			names.put(key, trimmed);
			setDirty();
		}
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		ListTag entries = new ListTag();
		names.forEach((key, value) -> {
			CompoundTag entry = new CompoundTag();
			entry.putString("Key", key);
			entry.putString("Name", value);
			entries.add(entry);
		});
		tag.put("Entries", entries);
		return tag;
	}

	public static VoidNodeNamesData load(CompoundTag tag, HolderLookup.Provider registries) {
		VoidNodeNamesData data = new VoidNodeNamesData();
		ListTag entries = tag.getList("Entries", Tag.TAG_COMPOUND);
		for (Tag raw : entries) {
			CompoundTag entry = (CompoundTag) raw;
			data.names.put(entry.getString("Key"), entry.getString("Name"));
		}
		return data;
	}

}

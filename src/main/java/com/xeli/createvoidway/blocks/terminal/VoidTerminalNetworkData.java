package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Persists void-node-terminal network membership across chunk unload and server restarts.
 * In-memory {@link VoidTerminalNetworkHandler} alone drops unloaded terminals on dedicated servers.
 */
public class VoidTerminalNetworkData extends SavedData {

	private final List<NetworkEntry> entries = new ArrayList<>();

	private record NetworkEntry(ResourceLocation dimension, NetworkKey key, Set<Long> positions) {
	}

	public void add(HolderLookup.Provider registries, ResourceLocation dimension, NetworkKey key, BlockPos pos) {
		NetworkEntry entry = findOrCreate(dimension, key);
		if (entry.positions.add(pos.asLong()))
			setDirty();
	}

	public void remove(HolderLookup.Provider registries, ResourceLocation dimension, NetworkKey key, BlockPos pos) {
		Iterator<NetworkEntry> it = entries.iterator();
		while (it.hasNext()) {
			NetworkEntry entry = it.next();
			if (!entry.dimension.equals(dimension) || !entry.key.equals(key))
				continue;
			if (entry.positions.remove(pos.asLong())) {
				if (entry.positions.isEmpty())
					it.remove();
				setDirty();
			}
			return;
		}
	}

	public void removePosition(ResourceLocation dimension, BlockPos pos) {
		boolean changed = false;
		long packed = pos.asLong();
		Iterator<NetworkEntry> it = entries.iterator();
		while (it.hasNext()) {
			NetworkEntry entry = it.next();
			if (!entry.dimension.equals(dimension))
				continue;
			if (entry.positions.remove(packed)) {
				changed = true;
				if (entry.positions.isEmpty())
					it.remove();
			}
		}
		if (changed)
			setDirty();
	}

	public void collectPositions(NetworkKey key, BiConsumer<ResourceLocation, BlockPos> consumer) {
		for (NetworkEntry entry : entries) {
			if (!entry.key.equals(key))
				continue;
			for (Long packed : entry.positions)
				consumer.accept(entry.dimension, BlockPos.of(packed));
		}
	}

	private NetworkEntry findOrCreate(ResourceLocation dimension, NetworkKey key) {
		for (NetworkEntry entry : entries) {
			if (entry.dimension.equals(dimension) && entry.key.equals(key))
				return entry;
		}
		NetworkEntry created = new NetworkEntry(dimension, key, new LinkedHashSet<>());
		entries.add(created);
		return created;
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		ListTag list = new ListTag();
		for (NetworkEntry entry : entries) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putString("Dimension", entry.dimension.toString());
			entryTag.put("Network", entry.key.serialize(registries));
			ListTag positions = new ListTag();
			for (Long packed : entry.positions)
				positions.add(LongTag.valueOf(packed));
			entryTag.put("Positions", positions);
			list.add(entryTag);
		}
		tag.put("Entries", list);
		return tag;
	}

	public static VoidTerminalNetworkData load(CompoundTag tag, HolderLookup.Provider registries) {
		VoidTerminalNetworkData data = new VoidTerminalNetworkData();
		ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
		for (Tag raw : list) {
			CompoundTag entryTag = (CompoundTag) raw;
			ResourceLocation dimension = ResourceLocation.parse(entryTag.getString("Dimension"));
			NetworkKey key = NetworkKey.deserialize(registries, entryTag.getCompound("Network"));
			Set<Long> positions = new LinkedHashSet<>();
			ListTag positionsTag = entryTag.getList("Positions", Tag.TAG_LONG);
			for (Tag posTag : positionsTag)
				positions.add(((LongTag) posTag).getAsLong());
			if (!positions.isEmpty())
				data.entries.add(new NetworkEntry(dimension, key, positions));
		}
		return data;
	}

}

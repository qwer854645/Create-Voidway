package com.xeli.createvoidway.blocks.voidtypes;

import com.simibubi.create.Create;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.voidlink.VoidNetworkLevels;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.function.BiConsumer;

public class VoidStorageNetworkHandler {

	static final Map<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> connections = new HashMap<>();

	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = actor.getNetworkKey();
		return networksInWorld.computeIfAbsent(key, $ -> new LinkedHashSet<>());
	}

	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		return connections.computeIfAbsent(WorldHelper.getDimensionID(world), $ -> new HashMap<>());
	}

	public void onLoadWorld(LevelAccessor world) {
		networksIn(world);
		Create.LOGGER.debug("Prepared Void Storage Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		// Keep frequency indexes across dimension unload for cross-dimension partners.
	}

	public void clearAll() {
		connections.clear();
	}

	public void addToNetwork(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		getNetworkOf(world, actor).add(actor.getPos());
		updateNetworkOf(world, actor);
	}

	public void removeFromNetwork(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		NetworkKey key = actor.getNetworkKey();
		Set<BlockPos> network = getNetworkOf(world, actor);
		network.remove(actor.getPos());
		if (network.isEmpty())
			networksIn(world).remove(key);

		if (actor.blockEntity instanceof IVoidStorageRelay relay) {
			relay.setLinkedPartners(0);
			relay.setReadyPartners(0);
		}

		// Always refresh remaining members in other dimensions for this key.
		updateNetworkOf(world, actor);
	}

	public void updateNetworkOf(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		NetworkKey key = actor.getNetworkKey();
		pruneDeadLoadedPositions(world, key);

		collectPositions(key, (dimension, pos) -> {
			Level level = VoidNetworkLevels.resolve(world, dimension);
			if (level == null || !level.hasChunkAt(pos))
				return;
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof IVoidStorageRelay relay)
				relay.updateLinkedPartnerCount(level);
		});
	}

	public int countLinkedPartners(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		return countPartners(world, actor, false);
	}

	public int countReadyPartners(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		return countPartners(world, actor, true);
	}

	private int countPartners(LevelAccessor world, VoidStorageLinkBehaviour actor, boolean requireReady) {
		if (!(actor.blockEntity instanceof IVoidStorageRelay self))
			return 0;

		boolean wantOutputs = !self.isStorageOutput();
		VoidStorageKind kind = self.getStorageKind();
		ResourceLocation selfDimension = WorldHelper.getDimensionID(world);
		BlockPos selfPos = actor.getPos();
		int count = 0;

		for (Map.Entry<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> dimensionEntry : connections.entrySet()) {
			Set<BlockPos> positions = dimensionEntry.getValue().get(actor.getNetworkKey());
			if (positions == null)
				continue;
			ResourceLocation dimension = dimensionEntry.getKey();
			Level level = VoidNetworkLevels.resolve(world, dimension);
			if (level == null)
				continue;

			for (BlockPos pos : positions) {
				if (pos.equals(selfPos) && dimension.equals(selfDimension))
					continue;
				if (!VoidNetworkLevels.isLoadedAlive(level, pos))
					continue;
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (!(blockEntity instanceof IVoidStorageRelay relay))
					continue;
				if (relay.getStorageKind() != kind)
					continue;
				if (relay.isStorageOutput() != wantOutputs)
					continue;
				if (requireReady && !relay.isLocallyReady())
					continue;
				count++;
			}
		}
		return count;
	}

	private void pruneDeadLoadedPositions(LevelAccessor context, NetworkKey key) {
		for (Map.Entry<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> dimensionEntry : connections.entrySet()) {
			Set<BlockPos> positions = dimensionEntry.getValue().get(key);
			if (positions == null)
				continue;
			Level level = VoidNetworkLevels.resolve(context, dimensionEntry.getKey());
			if (level == null)
				continue;
			for (Iterator<BlockPos> iterator = positions.iterator(); iterator.hasNext(); ) {
				BlockPos pos = iterator.next();
				if (VoidNetworkLevels.shouldDropFromIndex(level, pos))
					iterator.remove();
			}
			if (positions.isEmpty())
				dimensionEntry.getValue().remove(key);
		}
	}

	public void collectPositions(NetworkKey key, BiConsumer<ResourceLocation, BlockPos> consumer) {
		for (Map.Entry<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> dimensionEntry : connections.entrySet()) {
			Set<BlockPos> positions = dimensionEntry.getValue().get(key);
			if (positions == null)
				continue;
			ResourceLocation dimension = dimensionEntry.getKey();
			for (BlockPos pos : positions)
				consumer.accept(dimension, pos);
		}
	}

}

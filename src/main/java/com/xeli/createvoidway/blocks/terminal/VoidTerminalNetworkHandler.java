package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.Create;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class VoidTerminalNetworkHandler {

	static final Map<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> connections = new HashMap<>();

	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidTerminalLinkBehaviour actor) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = actor.getNetworkKey();
		return networksInWorld.computeIfAbsent(key, $ -> new LinkedHashSet<>());
	}

	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		return connections.computeIfAbsent(WorldHelper.getDimensionID(world), $ -> new HashMap<>());
	}

	public void onLoadWorld(LevelAccessor world) {
		networksIn(world);
		Create.LOGGER.debug("Prepared Void Terminal Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		connections.remove(WorldHelper.getDimensionID(world));
		Create.LOGGER.debug("Removed Void Terminal Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void addToNetwork(LevelAccessor world, VoidTerminalLinkBehaviour actor) {
		BlockPos pos = actor.getPos();
		NetworkKey key = actor.getNetworkKey();
		getNetworkOf(world, actor).add(pos);

		VoidTerminalNetworkData data = VoidwayMod.VOID_TERMINAL_NETWORK_DATA;
		if (data != null && world instanceof ServerLevel serverLevel)
			data.add(serverLevel.registryAccess(), WorldHelper.getDimensionID(world), key, pos);
	}

	public void removeFromNetwork(LevelAccessor world, VoidTerminalLinkBehaviour actor) {
		NetworkKey key = actor.getNetworkKey();
		BlockPos pos = actor.getPos();
		Set<BlockPos> network = getNetworkOf(world, actor);
		network.remove(pos);
		if (network.isEmpty())
			networksIn(world).remove(key);

		VoidTerminalNetworkData data = VoidwayMod.VOID_TERMINAL_NETWORK_DATA;
		if (data != null && world instanceof ServerLevel serverLevel)
			data.remove(serverLevel.registryAccess(), WorldHelper.getDimensionID(world), key, pos);
	}

	public void removeStalePosition(ServerLevel level, ResourceLocation dimension, BlockPos pos) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = connections.get(dimension);
		if (networksInWorld != null) {
			networksInWorld.values().forEach(set -> set.remove(pos));
			networksInWorld.entrySet().removeIf(entry -> entry.getValue().isEmpty());
		}
		VoidTerminalNetworkData data = VoidwayMod.VOID_TERMINAL_NETWORK_DATA;
		if (data != null)
			data.removePosition(dimension, pos);
	}

	public void collectPositions(ServerLevel anyServerLevel, NetworkKey key,
			BiConsumer<ResourceLocation, BlockPos> consumer) {
		VoidTerminalNetworkData data = VoidwayMod.VOID_TERMINAL_NETWORK_DATA;
		if (data != null) {
			data.collectPositions(key, consumer);
			return;
		}

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

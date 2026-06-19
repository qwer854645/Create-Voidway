package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.Create;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
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
		getNetworkOf(world, actor).add(actor.getPos());
	}

	public void removeFromNetwork(LevelAccessor world, VoidTerminalLinkBehaviour actor) {
		Set<BlockPos> network = getNetworkOf(world, actor);
		network.remove(actor.getPos());
		if (network.isEmpty())
			networksIn(world).remove(actor.getNetworkKey());
	}

	private static boolean isAlive(LevelAccessor world, BlockPos pos) {
		if (!world.hasChunkAt(pos))
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity != null && !blockEntity.isRemoved();
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

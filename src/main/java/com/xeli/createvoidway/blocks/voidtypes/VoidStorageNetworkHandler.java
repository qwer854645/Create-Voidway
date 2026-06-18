package com.xeli.createvoidway.blocks.voidtypes;

import com.simibubi.create.Create;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

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
		connections.remove(WorldHelper.getDimensionID(world));
		Create.LOGGER.debug("Removed Void Storage Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void addToNetwork(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		getNetworkOf(world, actor).add(actor.getPos());
		updateNetworkOf(world, actor);
	}

	public void removeFromNetwork(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		Set<BlockPos> network = getNetworkOf(world, actor);
		network.remove(actor.getPos());
		if (network.isEmpty())
			networksIn(world).remove(actor.getNetworkKey());
		else
			updateNetworkOf(world, actor);

		if (actor.blockEntity instanceof IVoidStorageRelay relay)
			relay.setLinkedPartners(0);
	}

	public void updateNetworkOf(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		Set<BlockPos> network = getNetworkOf(world, actor);

		for (Iterator<BlockPos> iterator = network.iterator(); iterator.hasNext(); ) {
			BlockPos pos = iterator.next();
			if (!isAlive(world, pos))
				iterator.remove();
		}

		for (BlockPos pos : network) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof IVoidStorageRelay relay)
				relay.updateLinkedPartnerCount(world);
		}
	}

	public int countLinkedPartners(LevelAccessor world, VoidStorageLinkBehaviour actor) {
		if (!(actor.blockEntity instanceof IVoidStorageRelay self))
			return 0;

		boolean wantOutputs = !self.isStorageOutput();
		VoidStorageKind kind = self.getStorageKind();
		int count = 0;
		BlockPos selfPos = actor.getPos();

		for (BlockPos pos : getNetworkOf(world, actor)) {
			if (pos.equals(selfPos) || !isAlive(world, pos))
				continue;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof IVoidStorageRelay relay))
				continue;
			if (relay.getStorageKind() != kind)
				continue;
			if (relay.isStorageOutput() != wantOutputs)
				continue;
			count++;
		}
		return count;
	}

	private static boolean isAlive(LevelAccessor world, BlockPos pos) {
		if (!world.hasChunkAt(pos))
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity != null && !blockEntity.isRemoved();
	}

}

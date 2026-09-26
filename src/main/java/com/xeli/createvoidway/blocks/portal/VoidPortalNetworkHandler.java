package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkMetrics;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.voidlink.VoidNetworkLevels;
import com.xeli.createvoidway.voidlink.VoidNetworkLevels.DimPos;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class VoidPortalNetworkHandler {

	public enum PairStatus {
		UNPAIRED,
		VALID,
		CONFLICT
	}

	static final Map<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> connections = new HashMap<>();
	private static final Map<ResourceLocation, Long> lastCooldownSweep = new HashMap<>();

	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidPortalLinkBehaviour link) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = link.getNetworkKey();
		return networksInWorld.computeIfAbsent(key, $ -> new LinkedHashSet<>());
	}

	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		return connections.computeIfAbsent(WorldHelper.getDimensionID(world), $ -> new HashMap<>());
	}

	public void onLoadWorld(LevelAccessor world) {
		networksIn(world);
		Create.LOGGER.debug("Prepared Void Portal Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		// Keep frequency indexes across dimension unload for cross-dimension partners.
		lastCooldownSweep.remove(WorldHelper.getDimensionID(world));
	}

	public void clearAll() {
		connections.clear();
		lastCooldownSweep.clear();
	}

	public void onPortalBlockChanged(LevelAccessor world, BlockPos pos) {
		VoidPortalShape shape = VoidPortalShape.findAt(world, pos);
		if (shape != null)
			refreshPortal(world, shape.connectorPos());
		invalidateNearbyPortals(world, pos);
	}

	private void invalidateNearbyPortals(LevelAccessor world, BlockPos changed) {
		for (Direction delta : Direction.values()) {
			BlockPos neighbor = changed.relative(delta);
			if (world.getBlockEntity(neighbor) instanceof VoidPortalConnectorTileEntity connector)
				connector.refreshShapeAndNetwork();
		}
	}

	public void refreshPortal(LevelAccessor world, BlockPos connectorPos) {
		if (world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity connector && !world.isClientSide())
			connector.updateCachedShape();

		VoidPortalLinkBehaviour link = getLinkBehaviour(world, connectorPos);
		if (link == null)
			return;

		Set<BlockPos> network = getNetworkOf(world, link);
		BlockPos previous = findRegisteredPos(network, connectorPos);

		boolean occupy = world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity connector
				&& connector.canOccupyFrequency();
		if (occupy)
			network.add(connectorPos);
		else
			network.remove(connectorPos);

		if (previous != null && !previous.equals(connectorPos))
			network.remove(previous);

		updateNetwork(world, link.getNetworkKey());

		if (world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity connector && !world.isClientSide()) {
			if (!network.contains(connectorPos))
				connector.setNetworkState(PairStatus.UNPAIRED, 0, null, null, 0);
			connector.refreshPortalBlocks();
		}
	}

	@Nullable
	private static BlockPos findRegisteredPos(Set<BlockPos> network, BlockPos connectorPos) {
		for (BlockPos pos : network) {
			if (pos.equals(connectorPos))
				return pos;
		}
		return null;
	}

	public void detachPortalFromNetwork(LevelAccessor world, BlockPos connectorPos) {
		VoidPortalLinkBehaviour link = getLinkBehaviour(world, connectorPos);
		if (link == null)
			return;
		Set<BlockPos> network = networksIn(world).get(link.getNetworkKey());
		if (network != null && network.remove(connectorPos))
			updateNetwork(world, link.getNetworkKey());
	}

	public void removePortalFromAllNetworks(LevelAccessor world, BlockPos connectorPos) {
		List<NetworkKey> affected = new ArrayList<>();
		for (var entry : networksIn(world).entrySet()) {
			if (entry.getValue().remove(connectorPos))
				affected.add(entry.getKey());
		}
		for (NetworkKey key : affected)
			updateNetwork(world, key);
		if (world.getBlockEntity(connectorPos) instanceof IVoidPortalEndpoint portal)
			portal.setNetworkState(PairStatus.UNPAIRED, 0, null, null, 0);
	}

	private void updateNetwork(LevelAccessor world, NetworkKey key) {
		pruneDeadLoadedPositions(world, key);

		List<DimPos> loaded = new ArrayList<>();
		List<DimPos> unloaded = new ArrayList<>();
		collectPositions(key, (dimension, pos) -> {
			Level level = VoidNetworkLevels.resolve(world, dimension);
			if (level == null || !level.hasChunkAt(pos)) {
				unloaded.add(new DimPos(dimension, pos));
				return;
			}
			if (!isValidPortal(level, pos))
				return;
			loaded.add(new DimPos(dimension, pos));
		});

		List<DimPos> members = resolvePairMembers(world, loaded, unloaded);
		PairStatus status = getPairStatus(members.size());
		int count = members.size();
		int linkDistance = 0;
		if (status == PairStatus.VALID && count == 2) {
			DimPos first = members.get(0);
			DimPos second = members.get(1);
			Level firstLevel = VoidNetworkLevels.resolve(world, first.dimension());
			linkDistance = VoidTeleportLinkMetrics.computeDistanceBlocks(
					firstLevel, first.pos(), second.dimension(), second.pos());
		}

		for (DimPos portalPos : members) {
			Level level = VoidNetworkLevels.resolve(world, portalPos.dimension());
			if (level == null || !level.hasChunkAt(portalPos.pos()))
				continue;
			DimPos partner = status == PairStatus.VALID ? findPartner(portalPos, members) : null;
			BlockEntity blockEntity = level.getBlockEntity(portalPos.pos());
			if (blockEntity instanceof IVoidPortalEndpoint portal)
				portal.setNetworkState(status, count,
						partner == null ? null : partner.dimension(),
						partner == null ? null : partner.pos(),
						linkDistance);
		}

		for (DimPos portalPos : members) {
			Level level = VoidNetworkLevels.resolve(world, portalPos.dimension());
			if (level != null && level.hasChunkAt(portalPos.pos())
					&& level.getBlockEntity(portalPos.pos()) instanceof VoidPortalConnectorTileEntity connector)
				connector.refreshPortalBlocks();
		}
	}

	private static List<DimPos> resolvePairMembers(LevelAccessor world, List<DimPos> loaded, List<DimPos> unloaded) {
		if (unloaded.isEmpty())
			return loaded;
		if (loaded.size() >= 2)
			return loaded;
		if (loaded.size() == 1) {
			DimPos self = loaded.get(0);
			DimPos stored = readStoredPartner(world, self);
			if (stored != null && !stored.equals(self))
				return List.of(self, stored);
			List<DimPos> combined = new ArrayList<>(loaded);
			combined.addAll(unloaded);
			return combined;
		}
		return unloaded;
	}

	@Nullable
	private static DimPos readStoredPartner(LevelAccessor world, DimPos self) {
		Level level = VoidNetworkLevels.resolve(world, self.dimension());
		if (level == null || !level.hasChunkAt(self.pos()))
			return null;
		if (!(level.getBlockEntity(self.pos()) instanceof VoidPortalConnectorTileEntity portal))
			return null;
		BlockPos partnerPos = portal.getPartnerPos();
		if (partnerPos == null)
			return null;
		ResourceLocation partnerDim = portal.getPartnerDimension();
		if (partnerDim == null)
			partnerDim = self.dimension();
		return new DimPos(partnerDim, partnerPos);
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
				if (!level.hasChunkAt(pos))
					continue;
				if (VoidNetworkLevels.shouldDropFromIndex(level, pos) || !isValidPortal(level, pos)) {
					if (level.getBlockEntity(pos) instanceof IVoidPortalEndpoint portal)
						portal.setNetworkState(PairStatus.UNPAIRED, 0, null, null, 0);
					iterator.remove();
				}
			}
			if (positions.isEmpty())
				dimensionEntry.getValue().remove(key);
		}
	}

	private static boolean isValidPortal(LevelAccessor world, BlockPos connectorPos) {
		if (!(world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity connector))
			return false;
		return connector.canOccupyFrequency();
	}

	private PairStatus getPairStatus(int size) {
		if (size < 2)
			return PairStatus.UNPAIRED;
		if (size > 2)
			return PairStatus.CONFLICT;
		return PairStatus.VALID;
	}

	public static boolean hasFrequencyConfigured(VoidPortalLinkBehaviour link) {
		return !link.getFrequencyStack(true).isEmpty() && !link.getFrequencyStack(false).isEmpty();
	}

	@Nullable
	private static VoidPortalLinkBehaviour getLinkBehaviour(LevelAccessor world, BlockPos pos) {
		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, VoidLinkBehaviour.TYPE);
		return behaviour instanceof VoidPortalLinkBehaviour portalLink ? portalLink : null;
	}

	@Nullable
	private static DimPos findPartner(DimPos self, List<DimPos> portals) {
		for (DimPos pos : portals) {
			if (!pos.equals(self))
				return pos;
		}
		return null;
	}

	public void tickCooldowns(ServerLevel level) {
		ResourceLocation dim = level.dimension().location();
		long time = level.getGameTime();
		if (lastCooldownSweep.getOrDefault(dim, -1L) == time)
			return;
		lastCooldownSweep.put(dim, time);

		for (Entity entity : level.getAllEntities()) {
			if (!VoidPortalHelper.hasContactCooldown(entity))
				continue;
			if (!touchesAnyPortal(level, entity))
				VoidPortalHelper.clearContactCooldown(entity);
		}
	}

	private static boolean touchesAnyPortal(LevelAccessor world, Entity entity) {
		Map<NetworkKey, Set<BlockPos>> networks = connections.get(WorldHelper.getDimensionID(world));
		if (networks == null)
			return false;
		for (Set<BlockPos> portals : networks.values()) {
			for (BlockPos pos : portals) {
				if (!world.hasChunkAt(pos))
					continue;
				VoidPortalShape shape = VoidPortalShape.findAt(world, pos);
				if (shape != null && world instanceof Level level
						&& VoidPortalHelper.isEntityTouchingPortalBlock(level, shape, entity))
					return true;
			}
		}
		return false;
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

package com.xeli.createvoidway.blocks.teleport;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.voidlink.VoidNetworkLevels;
import com.xeli.createvoidway.voidlink.VoidNetworkLevels.DimPos;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class VoidTeleportNetworkHandler {

	public enum PairStatus {
		UNPAIRED,
		VALID,
		CONFLICT
	}

	static final Map<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> connections = new HashMap<>();
	private static final Map<ResourceLocation, Long> lastCooldownSweep = new HashMap<>();

	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidTeleportLinkBehaviour link) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = link.getNetworkKey();
		return networksInWorld.computeIfAbsent(key, $ -> new LinkedHashSet<>());
	}

	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		return connections.computeIfAbsent(WorldHelper.getDimensionID(world), $ -> new HashMap<>());
	}

	public void onLoadWorld(LevelAccessor world) {
		networksIn(world);
		Create.LOGGER.debug("Prepared Void Teleport Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		// Keep frequency indexes across dimension unload for cross-dimension partners.
		lastCooldownSweep.remove(WorldHelper.getDimensionID(world));
	}

	public void clearAll() {
		connections.clear();
		lastCooldownSweep.clear();
	}

	public boolean tryBind(LevelAccessor world, BlockPos linkPos, BlockPos padPos) {
		if (!areAdjacent(linkPos, padPos))
			return false;
		if (!(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe))
			return false;
		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe))
			return false;

		unbindLink(world, linkPos);
		unbindPad(world, padPos);

		linkTe.setBoundPadPos(padPos);
		padTe.setBoundLinkPos(linkPos);
		syncPadsForLink(world, linkPos);
		return true;
	}

	public boolean unbindLink(LevelAccessor world, BlockPos linkPos) {
		if (!(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe))
			return false;
		BlockPos padPos = linkTe.getBoundPadPos();
		if (padPos == null)
			return false;

		linkTe.setBoundPadPos(null);
		if (world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe)
			padTe.setBoundLinkPos(null);

		removePadFromAllNetworks(world, padPos);
		return true;
	}

	public boolean unbindPad(LevelAccessor world, BlockPos padPos) {
		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe))
			return false;
		BlockPos linkPos = padTe.getBoundLinkPos();
		if (linkPos == null)
			return false;

		padTe.setBoundLinkPos(null);
		if (world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe)
			linkTe.setBoundPadPos(null);

		removePadFromAllNetworks(world, padPos);
		return true;
	}

	public void syncPadsForLink(LevelAccessor world, BlockPos linkPos) {
		VoidTeleportLinkTileEntity linkTe = getLinkTileEntity(world, linkPos);
		VoidTeleportLinkBehaviour link = linkTe == null ? null : linkTe.getTeleportLink();
		if (link == null)
			return;

		BlockPos padPos = linkTe.getBoundPadPos();
		Set<BlockPos> network = getNetworkOf(world, link);

		network.remove(linkPos);
		if (padPos != null)
			network.remove(padPos);

		if (hasFrequencyConfigured(link)) {
			// Index only the pad — never the link block — so unloaded entries are always pads.
			if (padPos != null && linkTe.isMutuallyBound())
				network.add(padPos);
		}

		updateNetwork(world, link.getNetworkKey());
	}

	public void removePadsForLink(LevelAccessor world, VoidTeleportLinkBehaviour link) {
		removePadsForLinkAt(world, link.getPos());
	}

	/** Removes the bound pad from the in-memory frequency network only (chunk unload / frequency change). */
	public void detachPadFromNetwork(LevelAccessor world, BlockPos linkPos) {
		VoidTeleportLinkTileEntity linkTe = getLinkTileEntity(world, linkPos);
		VoidTeleportLinkBehaviour link = linkTe == null ? null : linkTe.getTeleportLink();
		if (link == null)
			return;

		BlockPos padPos = linkTe.getBoundPadPos();
		if (padPos == null)
			return;

		Set<BlockPos> network = networksIn(world).get(link.getNetworkKey());
		if (network != null && network.remove(padPos))
			updateNetwork(world, link.getNetworkKey());
	}

	public void removePadsForLinkAt(LevelAccessor world, BlockPos linkPos) {
		unbindLink(world, linkPos);
	}

	public void onLinkNeighborChanged(LevelAccessor world, BlockPos linkPos) {
		validateLinkBinding(world, linkPos);
	}

	public void refreshPadBinding(LevelAccessor world, BlockPos padPos) {
		validatePadBinding(world, padPos);
	}

	public void validateLinkBinding(LevelAccessor world, BlockPos linkPos) {
		if (!(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe))
			return;

		BlockPos padPos = linkTe.getBoundPadPos();
		if (padPos == null) {
			syncPadsForLink(world, linkPos);
			return;
		}

		if (!world.hasChunkAt(padPos))
			return;

		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe)) {
			unbindLink(world, linkPos);
			return;
		}

		if (!linkTe.isMutuallyBound()) {
			if (padTe.isBoundTo(linkPos)) {
				syncPadsForLink(world, linkPos);
				return;
			}
			unbindLink(world, linkPos);
			return;
		}

		syncPadsForLink(world, linkPos);
	}

	public void validatePadBinding(LevelAccessor world, BlockPos padPos) {
		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe))
			return;

		BlockPos linkPos = padTe.getBoundLinkPos();
		if (linkPos == null) {
			padTe.setNetworkState(PairStatus.UNPAIRED, 0, null, null, 0);
			return;
		}

		if (!world.hasChunkAt(linkPos))
			return;

		if (!(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity)) {
			unbindPad(world, padPos);
			return;
		}

		if (!padTe.isMutuallyBound()) {
			if (world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe
					&& linkTe.isBoundTo(padPos)) {
				VoidTeleportLinkBehaviour link = findBindingLink(world, padPos);
				if (link != null)
					syncPadsForLink(world, link.getPos());
				return;
			}
			unbindPad(world, padPos);
			return;
		}

		VoidTeleportLinkBehaviour link = findBindingLink(world, padPos);
		if (link != null)
			syncPadsForLink(world, link.getPos());
	}

	public void removePadFromAllNetworks(LevelAccessor world, BlockPos padPos) {
		removePadFromAllNetworks(world, padPos, true);
	}

	private void removePadFromAllNetworks(LevelAccessor world, BlockPos padPos, boolean updateNetworks) {
		List<NetworkKey> affected = new ArrayList<>();
		for (var entry : networksIn(world).entrySet()) {
			if (entry.getValue().remove(padPos))
				affected.add(entry.getKey());
		}
		if (updateNetworks) {
			for (NetworkKey key : affected)
				updateNetwork(world, key);
			if (world.getBlockEntity(padPos) instanceof IVoidTeleportPad pad)
				pad.setNetworkState(PairStatus.UNPAIRED, 0, null, null, 0);
		}
	}

	public int countBoundPads(@Nullable LevelAccessor world, BlockPos linkPos) {
		if (world == null || !(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe))
			return 0;
		return linkTe.isMutuallyBound() ? 1 : 0;
	}

	public static boolean areAdjacent(BlockPos a, BlockPos b) {
		return a.distManhattan(b) == 1;
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
			if (!(level.getBlockEntity(pos) instanceof VoidTeleportPadTileEntity))
				return;
			if (!hasValidPadBinding(level, pos))
				return;
			loaded.add(new DimPos(dimension, pos));
		});

		List<DimPos> members = resolvePairMembers(world, loaded, unloaded);
		PairStatus status = getPairStatus(members.size(), world, members);
		int count = members.size();
		int linkDistance = 0;
		if (status == PairStatus.VALID && count == 2) {
			DimPos first = members.get(0);
			DimPos second = members.get(1);
			Level firstLevel = VoidNetworkLevels.resolve(world, first.dimension());
			linkDistance = VoidTeleportLinkMetrics.computeDistanceBlocks(
					firstLevel, first.pos(), second.dimension(), second.pos());
		}

		for (DimPos padPos : members) {
			Level level = VoidNetworkLevels.resolve(world, padPos.dimension());
			if (level == null || !level.hasChunkAt(padPos.pos()))
				continue;
			DimPos partner = status == PairStatus.VALID ? findPartner(padPos, members) : null;
			BlockEntity blockEntity = level.getBlockEntity(padPos.pos());
			if (blockEntity instanceof IVoidTeleportPad pad)
				pad.setNetworkState(status, count,
						partner == null ? null : partner.dimension(),
						partner == null ? null : partner.pos(),
						linkDistance);
		}
	}

	/**
	 * Prefer loaded pads. When exactly one pad is loaded, trust its stored partner
	 * (covers unloaded cross-dim partners and ignores stale pre-0.2.13 link indexes).
	 */
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
		if (!(level.getBlockEntity(self.pos()) instanceof VoidTeleportPadTileEntity pad))
			return null;
		BlockPos partnerPos = pad.getPartnerPos();
		if (partnerPos == null)
			return null;
		ResourceLocation partnerDim = pad.getPartnerDimension();
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
				if (VoidNetworkLevels.shouldDropFromIndex(level, pos)
						|| !(level.getBlockEntity(pos) instanceof VoidTeleportPadTileEntity)
						|| !hasValidPadBinding(level, pos))
					iterator.remove();
			}
			if (positions.isEmpty())
				dimensionEntry.getValue().remove(key);
		}
	}

	private PairStatus getPairStatus(int size, LevelAccessor world, List<DimPos> pads) {
		if (size < 2)
			return PairStatus.UNPAIRED;
		if (size > 2)
			return PairStatus.CONFLICT;
		if (!networkHasCompleteFrequency(world, pads))
			return PairStatus.UNPAIRED;
		return PairStatus.VALID;
	}

	private static boolean networkHasCompleteFrequency(LevelAccessor world, List<DimPos> pads) {
		for (DimPos padPos : pads) {
			Level level = VoidNetworkLevels.resolve(world, padPos.dimension());
			if (level == null || !level.hasChunkAt(padPos.pos()))
				continue; // Unloaded pads stay trusted while indexed.
			if (!hasFrequencyConfiguredForPad(level, padPos.pos()))
				return false;
		}
		return true;
	}

	private static boolean hasValidPadBinding(LevelAccessor world, BlockPos padPos) {
		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe))
			return false;
		BlockPos linkPos = padTe.getBoundLinkPos();
		if (linkPos == null)
			return false;
		if (!world.hasChunkAt(linkPos))
			return true;
		if (!(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe))
			return false;
		return padTe.isBoundTo(linkPos) && linkTe.isBoundTo(padPos) && areAdjacent(padPos, linkPos);
	}

	private static boolean hasFrequencyConfiguredForPad(LevelAccessor world, BlockPos padPos) {
		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe))
			return false;
		BlockPos linkPos = padTe.getBoundLinkPos();
		if (linkPos == null)
			return false;
		if (!world.hasChunkAt(linkPos))
			return false;
		VoidTeleportLinkBehaviour link = getLinkBehaviour(world, linkPos);
		return link != null && hasFrequencyConfigured(link);
	}

	public static boolean hasFrequencyConfigured(VoidTeleportLinkBehaviour link) {
		return !link.getFrequencyStack(true).isEmpty() && !link.getFrequencyStack(false).isEmpty();
	}

	@Nullable
	public static VoidTeleportLinkBehaviour findBindingLink(LevelAccessor world, BlockPos padPos) {
		if (!(world.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe))
			return null;
		BlockPos linkPos = padTe.getBoundLinkPos();
		if (linkPos == null)
			return null;
		if (!(world.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe))
			return null;
		if (!linkTe.isBoundTo(padPos))
			return null;
		if (!areAdjacent(padPos, linkPos))
			return null;
		return getLinkBehaviour(world, linkPos);
	}

	@Nullable
	private static VoidTeleportLinkTileEntity getLinkTileEntity(LevelAccessor world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity instanceof VoidTeleportLinkTileEntity link ? link : null;
	}

	@Nullable
	private static VoidTeleportLinkBehaviour getLinkBehaviour(LevelAccessor world, BlockPos pos) {
		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, VoidLinkBehaviour.TYPE);
		return behaviour instanceof VoidTeleportLinkBehaviour teleportLink ? teleportLink : null;
	}

	@Nullable
	private static DimPos findPartner(DimPos self, List<DimPos> pads) {
		for (DimPos pos : pads) {
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
			if (!VoidTeleportHelper.hasContactCooldown(entity))
				continue;
			if (!touchesAnyPadZone(level, entity))
				VoidTeleportHelper.clearContactCooldown(entity);
		}
	}

	private static boolean touchesAnyPadZone(LevelAccessor world, Entity entity) {
		Map<NetworkKey, Set<BlockPos>> networks = connections.get(WorldHelper.getDimensionID(world));
		if (networks == null)
			return false;
		for (Set<BlockPos> pads : networks.values()) {
			for (BlockPos pos : pads) {
				if (!world.hasChunkAt(pos))
					continue;
				BlockEntity be = world.getBlockEntity(pos);
				if (be instanceof VoidTeleportPadTileEntity pad && pad.isEntityInContact(entity))
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

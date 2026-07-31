package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportPadTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class VoidNodeDiscovery {

	private VoidNodeDiscovery() {
	}

	public static List<VoidNodeNetworkIndex.DimensionalBlockPos> collectValidTerminals(ServerLevel terminalLevel,
			NetworkKey key) {
		return collectValidTerminals(terminalLevel, key, false);
	}

	/**
	 * @param forceLoad when true, loads remote chunks to validate membership (player GUI / teleport).
	 *                  when false, trusts persisted unloaded positions for stress accounting without chunk I/O.
	 */
	public static List<VoidNodeNetworkIndex.DimensionalBlockPos> collectValidTerminals(ServerLevel terminalLevel,
			NetworkKey key, boolean forceLoad) {
		List<VoidNodeNetworkIndex.DimensionalBlockPos> terminals = new ArrayList<>();

		for (VoidNodeNetworkIndex.DimensionalBlockPos node : VoidNodeNetworkIndex.collectPositions(terminalLevel, key)) {
			ServerLevel level = terminalLevel.getServer()
					.getLevel(ResourceKey.create(Registries.DIMENSION, node.dimension()));
			if (level == null)
				continue;

			if (!forceLoad && !level.hasChunkAt(node.pos())) {
				terminals.add(node);
				continue;
			}

			if (forceLoad)
				level.getChunkAt(node.pos());
			else if (!level.hasChunkAt(node.pos()))
				continue;

			if (!validateAndRepairMembership(level, node, key, forceLoad))
				continue;

			terminals.add(node);
		}

		return terminals;
	}

	/**
	 * Returns true if {@code node} is a live terminal on {@code key}.
	 * Soft-repairs index instead of wiping on transient mismatches (common on dedicated servers).
	 */
	private static boolean validateAndRepairMembership(ServerLevel level,
			VoidNodeNetworkIndex.DimensionalBlockPos node, NetworkKey key, boolean allowMutation) {
		VoidLinkBehaviour link = resolveLink(level, node.pos());
		if (link == null) {
			BlockState state = level.getBlockState(node.pos());
			if (state.isAir() || VoidNodeType.fromBlockState(state) == VoidNodeType.UNKNOWN) {
				if (allowMutation)
					VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.removeStalePosition(level, node.dimension(), node.pos());
			}
			return false;
		}

		if (link.getFrequencyStack(true).isEmpty() || link.getFrequencyStack(false).isEmpty()) {
			if (allowMutation)
				VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.removeStalePosition(level, node.dimension(), node.pos());
			return false;
		}

		BlockState state = level.getBlockState(node.pos());
		VoidNodeType type = VoidNodeType.fromBlockState(state);
		if (!VoidNodeType.isTerminalDestination(type)) {
			if (allowMutation)
				VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.removeStalePosition(level, node.dimension(), node.pos());
			return false;
		}

		NetworkKey liveKey = link.getNetworkKey();
		if (!liveKey.equals(key)) {
			if (allowMutation && link instanceof VoidTerminalLinkBehaviour terminalLink) {
				VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.removeStalePosition(level, node.dimension(), node.pos());
				VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.addToNetwork(level, terminalLink);
			}
			return false;
		}

		// Re-affirm membership so SavedData cannot drift from the live block.
		if (allowMutation && link instanceof VoidTerminalLinkBehaviour terminalLink)
			VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.addToNetwork(level, terminalLink);

		return true;
	}

	public static List<VoidNodeEntry> listNodes(ServerLevel terminalLevel, NetworkKey key, BlockPos terminalPos) {
		return listNodes(terminalLevel, key, terminalPos, true);
	}

	/**
	 * @param forceIncludeReference when true and {@code referencePos} is a real terminal, always
	 *                              include it (station GUI). Portable UIs must pass false so the
	 *                              player's feet are not injected as a fake node.
	 */
	public static List<VoidNodeEntry> listNodes(ServerLevel terminalLevel, NetworkKey key, BlockPos referencePos,
			boolean forceIncludeReference) {
		ResourceLocation terminalDimension = WorldHelper.getDimensionID(terminalLevel);
		VoidNodeNamesData names = VoidwayMod.VOID_NODE_NAMES_DATA;
		List<VoidNodeEntry> entries = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (VoidNodeNetworkIndex.DimensionalBlockPos node : collectValidTerminals(terminalLevel, key, true))
			addEntry(entries, seen, names, terminalLevel, terminalDimension, referencePos, node);

		if (forceIncludeReference) {
			VoidNodeTerminalTileEntity self = null;
			BlockPos base = VoidNodeTerminalMultiblock.getBasePos(terminalLevel, referencePos);
			if (base == null)
				base = referencePos;
			if (terminalLevel.getBlockEntity(base) instanceof VoidNodeTerminalTileEntity terminal)
				self = terminal;
			if (self != null) {
				ResourceLocation refDim = WorldHelper.getDimensionID(terminalLevel);
				addEntry(entries, seen, names, terminalLevel, terminalDimension, referencePos,
						new VoidNodeNetworkIndex.DimensionalBlockPos(refDim, self.getBlockPos()));
			}
		}

		entries.sort(Comparator
				.comparing(VoidNodeEntry::currentTerminal).reversed()
				.thenComparing(VoidNodeEntry::dimension)
				.thenComparing(e -> e.displayName().toLowerCase(Locale.ROOT)));
		return entries;
	}

	private static void addEntry(List<VoidNodeEntry> entries, Set<String> seen, VoidNodeNamesData names,
			ServerLevel terminalLevel, ResourceLocation terminalDimension, BlockPos terminalPos,
			VoidNodeNetworkIndex.DimensionalBlockPos node) {
		String id = node.dimension() + "|" + node.pos().asLong();
		if (!seen.add(id))
			return;

		ServerLevel level = terminalLevel.getServer()
				.getLevel(ResourceKey.create(Registries.DIMENSION, node.dimension()));
		BlockState state = level != null ? level.getBlockState(node.pos()) : null;
		VoidNodeType type = state != null ? VoidNodeType.fromBlockState(state) : VoidNodeType.UNKNOWN;
		String customName = names != null ? names.getName(node.dimension(), node.pos()) : null;
		String renameName = customName != null && !customName.isBlank()
				? customName
				: buildDefaultName(type, node.pos());
		String displayName = withDimensionPrefix(renameName, node.dimension(), terminalDimension);
		boolean currentTerminal = node.dimension().equals(terminalDimension) && node.pos().equals(terminalPos);
		int distanceBlocks = computeDistanceBlocks(terminalLevel, terminalDimension, terminalPos,
				node.dimension(), node.pos());
		entries.add(new VoidNodeEntry(node.dimension(), node.pos(), type.name(), displayName, renameName,
				currentTerminal, distanceBlocks));
	}

	/** Re-registers a terminal into the network index (memory + SavedData). */
	public static void ensureIndexed(VoidNodeTerminalTileEntity terminal) {
		if (!(terminal.getLevel() instanceof ServerLevel level))
			return;
		VoidTerminalLinkBehaviour link = terminal.getLink();
		if (link == null)
			return;
		if (link.getFrequencyStack(true).isEmpty() || link.getFrequencyStack(false).isEmpty())
			return;
		VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER.addToNetwork(level, link);
	}

	@Nullable
	public static VoidLinkBehaviour resolveLink(ServerLevel level, BlockPos pos) {
		BlockPos basePos = VoidNodeTerminalMultiblock.getBasePos(level, pos);
		if (basePos != null)
			pos = basePos;

		VoidLinkBehaviour link = BlockEntityBehaviour.get(level, pos, VoidLinkBehaviour.TYPE);
		if (link != null)
			return link;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof VoidTeleportPadTileEntity pad) {
			BlockPos linkPos = pad.getBoundLinkPos();
			if (linkPos != null)
				return BlockEntityBehaviour.get(level, linkPos, VoidLinkBehaviour.TYPE);
		}
		return null;
	}

	private static String buildDefaultName(VoidNodeType type, BlockPos pos) {
		String typeText = type == VoidNodeType.UNKNOWN
				? type.name()
				: Component.translatable("block.createvoidway." + type.getBlockKey()).getString();
		return typeText + " (" + VoidNodeType.formatCoords(pos) + ")";
	}

	private static String withDimensionPrefix(String base, ResourceLocation dimension, ResourceLocation terminalDimension) {
		if (dimension.equals(terminalDimension))
			return base;
		return dimensionLabel(dimension) + " \u203A " + base;
	}

	private static int computeDistanceBlocks(ServerLevel level, ResourceLocation fromDimension, BlockPos fromPos,
			ResourceLocation toDimension, BlockPos toPos) {
		if (!fromDimension.equals(toDimension))
			return VoidNodeEntry.DISTANCE_OTHER_DIMENSION;
		return distanceBlocks(level, fromPos, toPos);
	}

	public static int distanceBlocks(ServerLevel level, BlockPos fromPos, BlockPos toPos) {
		return com.xeli.createvoidway.compat.VoidwaySableCompat.distanceBlocks(level, fromPos, toPos);
	}

	public static String dimensionLabel(ResourceLocation dimension) {
		return Component.translatable(dimensionTranslationKey(dimension)).getString();
	}

	public static String dimensionTranslationKey(ResourceLocation dimension) {
		return "dimension." + dimension.getNamespace() + "." + dimension.getPath();
	}

}

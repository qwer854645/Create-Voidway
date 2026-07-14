package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.items.PortableVoidTerminalBinding;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodePlayerListPacket;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class VoidNodeService {

	private VoidNodeService() {
	}

	public static void sendNodeList(ServerPlayer player, VoidNodeTerminalTileEntity terminal) {
		if (terminal.getLevel() instanceof ServerLevel level) {
			List<VoidNodeEntry> nodes = VoidNodeDiscovery.listNodes(level, terminal.getNetworkKey(), terminal.getBlockPos());
			PacketDistributor.sendToPlayer(player, new VoidNodeListPacket(terminal.getBlockPos(), nodes));
		}
	}

	public static void sendPlayerList(ServerPlayer player, VoidNodeTerminalTileEntity terminal) {
		if (!(terminal.getLevel() instanceof ServerLevel level))
			return;

		ResourceLocation terminalDimension = WorldHelper.getDimensionID(level);
		BlockPos terminalPos = terminal.getBlockPos();

		Optional<GlobalPos> deathLocation = player.getLastDeathLocation();
		boolean hasDeathLocation = deathLocation.isPresent();
		int deathDistance = deathLocation
				.map(pos -> computeDistanceBlocks(level, terminalDimension, terminalPos, pos.dimension().location(), pos.pos()))
				.orElse(VoidTerminalPlayerEntry.DISTANCE_OTHER_DIMENSION);

		List<VoidTerminalPlayerEntry> players = new ArrayList<>();
		for (ServerPlayer online : player.server.getPlayerList().getPlayers()) {
			if (online.getUUID().equals(player.getUUID()))
				continue;
			ResourceLocation dimension = WorldHelper.getDimensionID(online.level());
			int distance = computeDistanceBlocks(level, terminalDimension, terminalPos, dimension,
					online.blockPosition());
			players.add(new VoidTerminalPlayerEntry(online.getUUID(), online.getGameProfile().getName(), dimension,
					distance));
		}

		players.sort(Comparator.comparing(VoidTerminalPlayerEntry::displayName, String.CASE_INSENSITIVE_ORDER));
		PacketDistributor.sendToPlayer(player,
				new VoidNodePlayerListPacket(terminal.getBlockPos(), hasDeathLocation, deathDistance, players));
	}

	public static void sendPortableNodeList(ServerPlayer player, InteractionHand hand, NetworkKey networkKey) {
		if (!PortableVoidTerminalBinding.canInteract(player, networkKey))
			return;
		if (!PortableVoidTerminalBinding.isComplete(networkKey))
			return;
		List<VoidNodeEntry> nodes = VoidNodeDiscovery.listNodes(player.serverLevel(), networkKey, player.blockPosition());
		PacketDistributor.sendToPlayer(player, new PortableVoidTerminalListPacket(hand, networkKey, nodes));
	}

	public static boolean bindPortableTerminal(ServerPlayer player, InteractionHand hand,
			VoidNodeTerminalTileEntity terminal) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(RWItems.PORTABLE_VOID_TERMINAL.get()))
			return false;
		if (!ensureCanOperate(player, terminal))
			return false;

		NetworkKey networkKey = terminal.getNetworkKey();
		if (!PortableVoidTerminalBinding.isComplete(networkKey)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.frequency_incomplete"),
					true);
			return false;
		}

		PortableVoidTerminalBinding.write(stack, networkKey, player.registryAccess());
		player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.bound"), true);
		return true;
	}

	public static boolean renameNode(ServerPlayer player, BlockPos terminalPos, ResourceLocation targetDimension,
			BlockPos targetPos, String newName) {
		VoidNodeTerminalTileEntity terminal = resolveTerminal(player.serverLevel(), terminalPos);
		if (terminal == null)
			return false;
		if (!ensureCanOperate(player, terminal))
			return false;
		if (!isSameNetwork(player.server, terminal.getNetworkKey(), targetDimension, targetPos)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}

		VoidwayMod.VOID_NODE_NAMES_DATA.setName(targetDimension, targetPos, newName);
		sendNodeList(player, terminal);
		return true;
	}

	public static boolean teleportPlayer(ServerPlayer player, BlockPos terminalPos, ResourceLocation targetDimension,
			BlockPos targetPos) {
		VoidNodeTerminalTileEntity terminal = resolveTerminal(player.serverLevel(), terminalPos);
		if (terminal == null)
			return false;
		if (!canInitiateSharedTeleport(player, terminal))
			return false;

		ResourceLocation terminalDimension = WorldHelper.getDimensionID(terminal.getLevel());
		BlockPos sourcePos = terminal.getBlockPos();
		if (terminalDimension.equals(targetDimension) && sourcePos.equals(targetPos)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.cannot_teleport_self"), true);
			return false;
		}
		if (!isSameNetwork(player.server, terminal.getNetworkKey(), targetDimension, targetPos)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}

		ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension));
		if (targetLevel == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.dimension_unavailable"), true);
			return false;
		}

		VoidNodeTerminalTileEntity targetTerminal = resolveTerminal(targetLevel, targetPos);
		if (targetTerminal != null && targetTerminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.target_cooldown"), true);
			return false;
		}

		int cost = VoidwayConfig.getVoidNodeTerminalTeleportFluidCostMb();
		if (terminal.getFluidTank().getFluidAmount() < cost) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_fluid"), true);
			return false;
		}
		terminal.getFluidTank().drain(cost, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

		if (!teleportPlayerToTerminal(player, targetLevel, targetPos))
			return false;

		targetLevel.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	public static boolean teleportToDeath(ServerPlayer player, BlockPos terminalPos) {
		VoidNodeTerminalTileEntity terminal = resolveTerminal(player.serverLevel(), terminalPos);
		if (terminal == null)
			return false;
		if (!canInitiateSharedTeleport(player, terminal))
			return false;

		Optional<GlobalPos> deathLocation = player.getLastDeathLocation();
		if (deathLocation.isEmpty()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.no_death_location"), true);
			return false;
		}

		GlobalPos death = deathLocation.get();
		ServerLevel targetLevel = player.server.getLevel(death.dimension());
		if (targetLevel == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.dimension_unavailable"), true);
			return false;
		}

		if (!drainTeleportFluid(terminal, player))
			return false;

		Vec3 target = VoidTerminalTeleportLanding.findTeleportPos(targetLevel, death.pos(), player);
		player.teleportTo(targetLevel, target.x, target.y, target.z, java.util.Collections.emptySet(),
				player.getYRot(), player.getXRot());
		VoidwaySableCompat.inheritSubLevelVelocity(targetLevel, player, target);
		targetLevel.playSound(null, death.pos(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	public static boolean teleportToPlayer(ServerPlayer player, BlockPos terminalPos, UUID targetPlayerUuid) {
		VoidNodeTerminalTileEntity terminal = resolveTerminal(player.serverLevel(), terminalPos);
		if (terminal == null)
			return false;
		if (!canInitiateSharedTeleport(player, terminal))
			return false;
		if (player.getUUID().equals(targetPlayerUuid)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.cannot_teleport_self"), true);
			return false;
		}

		ServerPlayer targetPlayer = player.server.getPlayerList().getPlayer(targetPlayerUuid);
		if (targetPlayer == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.player_unavailable"), true);
			return false;
		}

		ServerLevel targetLevel = targetPlayer.serverLevel();
		if (!drainTeleportFluid(terminal, player))
			return false;

		Vec3 target = VoidTerminalTeleportLanding.findTeleportPos(targetLevel, targetPlayer.blockPosition(), player);
		player.teleportTo(targetLevel, target.x, target.y, target.z, java.util.Collections.emptySet(),
				player.getYRot(), player.getXRot());
		VoidwaySableCompat.inheritSubLevelVelocity(targetLevel, player, target);
		targetLevel.playSound(null, targetPlayer.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	/** Teleports: any player may use an operational terminal. */
	private static boolean canInitiateSharedTeleport(ServerPlayer player, VoidNodeTerminalTileEntity terminal) {
		if (!ensureCanOperate(player, terminal))
			return false;
		if (terminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.teleport_cooldown"), true);
			return false;
		}
		return true;
	}

	private static boolean ensureCanOperate(ServerPlayer player, VoidNodeTerminalTileEntity terminal) {
		if (!terminal.canOperate()) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.terminal_unavailable"),
					true);
			return false;
		}
		return true;
	}

	private static boolean drainTeleportFluid(VoidNodeTerminalTileEntity terminal, ServerPlayer player) {
		int cost = VoidwayConfig.getVoidNodeTerminalTeleportFluidCostMb();
		if (terminal.getFluidTank().getFluidAmount() < cost) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_fluid"), true);
			return false;
		}
		terminal.getFluidTank().drain(cost, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
		return true;
	}

	private static int computeDistanceBlocks(ServerLevel level, ResourceLocation fromDimension, BlockPos fromPos,
			ResourceLocation toDimension, BlockPos toPos) {
		if (!fromDimension.equals(toDimension))
			return VoidTerminalPlayerEntry.DISTANCE_OTHER_DIMENSION;
		return VoidNodeDiscovery.distanceBlocks(level, fromPos, toPos);
	}

	public static boolean teleportViaPortable(ServerPlayer player, InteractionHand hand, NetworkKey networkKey,
			ResourceLocation targetDimension, BlockPos targetPos) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(RWItems.PORTABLE_VOID_TERMINAL.get()))
			return false;

		Optional<NetworkKey> boundKey = PortableVoidTerminalBinding.read(stack, player.registryAccess())
				.filter(key -> key.equals(networkKey));
		if (boundKey.isEmpty()) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.unbound"), true);
			return false;
		}
		if (!isSameNetwork(player.server, networkKey, targetDimension, targetPos)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}

		ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension));
		if (targetLevel == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.dimension_unavailable"), true);
			return false;
		}

		VoidNodeTerminalTileEntity targetTerminal = resolveTerminal(targetLevel, targetPos);
		if (targetTerminal == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}
		if (targetTerminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.target_cooldown"), true);
			return false;
		}

		int cost = VoidwayConfig.getPortableVoidTerminalTeleportFluidCostMb();
		if (targetTerminal.getFluidTank().getFluidAmount() < cost) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_fluid"), true);
			return false;
		}
		targetTerminal.getFluidTank().drain(cost, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

		if (!teleportPlayerToTerminal(player, targetLevel, targetPos))
			return false;

		targetTerminal.startPortableTeleportCooldown();
		targetLevel.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	private static boolean teleportPlayerToTerminal(ServerPlayer player, ServerLevel targetLevel, BlockPos targetPos) {
		targetLevel.getChunkAt(targetPos);
		BlockState targetState = targetLevel.getBlockState(targetPos);
		if (!targetState.is(RWBlocks.VOID_NODE_TERMINAL.get())) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}

		Vec3 target = VoidNodeTerminalLanding.findTeleportPos(targetLevel, targetPos, player);
		player.teleportTo(targetLevel, target.x, target.y, target.z, java.util.Collections.emptySet(),
				player.getYRot(), player.getXRot());
		VoidwaySableCompat.inheritSubLevelVelocity(targetLevel, player, target);
		return true;
	}

	@Nullable
	public static VoidNodeTerminalTileEntity resolveTerminal(ServerLevel level, BlockPos pos) {
		level.getChunkAt(pos);
		BlockPos basePos = VoidNodeTerminalMultiblock.getBasePos(level, pos);
		if (basePos == null)
			basePos = pos;
		if (level.getBlockEntity(basePos) instanceof VoidNodeTerminalTileEntity terminal)
			return terminal;
		return null;
	}

	private static boolean isSameNetwork(net.minecraft.server.MinecraftServer server, NetworkKey terminalKey,
			ResourceLocation targetDimension, BlockPos targetPos) {
		ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension));
		if (level == null)
			return false;
		level.getChunkAt(targetPos);
		VoidLinkBehaviour targetLink = VoidNodeDiscovery.resolveLink(level, targetPos);
		if (targetLink == null)
			return false;
		if (!targetLink.getNetworkKey().equals(terminalKey))
			return false;
		if (targetLink.getFrequencyStack(true).isEmpty() || targetLink.getFrequencyStack(false).isEmpty())
			return false;
		return true;
	}

}

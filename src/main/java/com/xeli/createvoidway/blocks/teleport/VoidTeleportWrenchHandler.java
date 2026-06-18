package com.xeli.createvoidway.blocks.teleport;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.xeli.createvoidway.VoidwayMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class VoidTeleportWrenchHandler {

	private static final String PENDING_POS_KEY = "CreateVoidwayTeleportBindPendingPos";
	private static final String PENDING_PAD_KEY = "CreateVoidwayTeleportBindPendingPad";

	private VoidTeleportWrenchHandler() {}

	public static InteractionResult onWrenchedLink(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos linkPos = context.getClickedPos();
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (level.getBlockEntity(linkPos) instanceof VoidTeleportLinkTileEntity linkTe && linkTe.isMutuallyBound()) {
			if (VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.unbindLink(level, linkPos))
				notify(player, "void_teleport_bind.unbound");
			clearPending(player);
			IWrenchable.playRotateSound(level, linkPos);
			return InteractionResult.SUCCESS;
		}

		BlockPos pendingPad = getPendingPad(player);
		if (pendingPad != null) {
			if (VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.tryBind(level, linkPos, pendingPad))
				notify(player, "void_teleport_bind.bound");
			else
				notify(player, "void_teleport_bind.not_adjacent");
			clearPending(player);
			IWrenchable.playRotateSound(level, linkPos);
			return InteractionResult.SUCCESS;
		}

		setPendingLink(player, linkPos);
		notify(player, "void_teleport_bind.select_pad");
		IWrenchable.playRotateSound(level, linkPos);
		return InteractionResult.SUCCESS;
	}

	public static InteractionResult onWrenchedPad(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos padPos = context.getClickedPos();
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (level.getBlockEntity(padPos) instanceof VoidTeleportPadTileEntity padTe && padTe.isMutuallyBound()) {
			if (VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.unbindPad(level, padPos))
				notify(player, "void_teleport_bind.unbound");
			clearPending(player);
			IWrenchable.playRotateSound(level, padPos);
			return InteractionResult.SUCCESS;
		}

		BlockPos pendingLink = getPendingLink(player);
		if (pendingLink != null) {
			if (VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.tryBind(level, pendingLink, padPos))
				notify(player, "void_teleport_bind.bound");
			else
				notify(player, "void_teleport_bind.not_adjacent");
			clearPending(player);
			IWrenchable.playRotateSound(level, padPos);
			return InteractionResult.SUCCESS;
		}

		setPendingPad(player, padPos);
		notify(player, "void_teleport_bind.select_link");
		IWrenchable.playRotateSound(level, padPos);
		return InteractionResult.SUCCESS;
	}

	private static void notify(Player player, String key) {
		player.displayClientMessage(Component.translatable("createvoidway." + key), true);
	}

	@Nullable
	private static BlockPos getPendingLink(Player player) {
		if (!player.getPersistentData().contains(PENDING_POS_KEY))
			return null;
		return BlockPos.of(player.getPersistentData().getLong(PENDING_POS_KEY));
	}

	@Nullable
	private static BlockPos getPendingPad(Player player) {
		if (!player.getPersistentData().contains(PENDING_PAD_KEY))
			return null;
		return BlockPos.of(player.getPersistentData().getLong(PENDING_PAD_KEY));
	}

	private static void setPendingLink(Player player, BlockPos pos) {
		player.getPersistentData().remove(PENDING_PAD_KEY);
		player.getPersistentData().putLong(PENDING_POS_KEY, pos.asLong());
	}

	private static void setPendingPad(Player player, BlockPos pos) {
		player.getPersistentData().remove(PENDING_POS_KEY);
		player.getPersistentData().putLong(PENDING_PAD_KEY, pos.asLong());
	}

	private static void clearPending(Player player) {
		player.getPersistentData().remove(PENDING_POS_KEY);
		player.getPersistentData().remove(PENDING_PAD_KEY);
	}

}

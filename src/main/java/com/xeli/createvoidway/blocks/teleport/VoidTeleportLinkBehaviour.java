package com.xeli.createvoidway.blocks.teleport;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

public class VoidTeleportLinkBehaviour extends VoidLinkBehaviour {

	public VoidTeleportLinkBehaviour(SmartBlockEntity te,
									 Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots) {
		super(te, slots);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().isClientSide())
			return;
		getHandler().syncPadsForLink(getWorld(), getPos());
	}

	@Override
	public void unload() {
		if (!getWorld().isClientSide())
			getHandler().detachPadFromNetwork(getWorld(), getPos());
		super.unload();
	}

	@Override
	protected void onLeaveNetwork() {
		getHandler().detachPadFromNetwork(getWorld(), getPos());
	}

	@Override
	protected void onJoinNetwork() {
		getHandler().syncPadsForLink(getWorld(), getPos());
	}

	public BlockPos getPos() {
		return blockEntity.getBlockPos();
	}

	public boolean isAlive() {
		Level level = getWorld();
		if (blockEntity.isRemoved() || !level.isLoaded(getPos()))
			return false;
		return level.getBlockEntity(getPos()) == blockEntity;
	}

	private VoidTeleportNetworkHandler getHandler() {
		return VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER;
	}

}

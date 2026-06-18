package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

public class VoidPortalLinkBehaviour extends VoidLinkBehaviour {

	public VoidPortalLinkBehaviour(SmartBlockEntity te,
								   Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots) {
		super(te, slots);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().isClientSide())
			return;
		getHandler().refreshPortal(getWorld(), getPos());
	}

	@Override
	public void unload() {
		if (!getWorld().isClientSide())
			getHandler().detachPortalFromNetwork(getWorld(), getPos());
		super.unload();
	}

	@Override
	protected void onLeaveNetwork() {
		getHandler().detachPortalFromNetwork(getWorld(), getPos());
	}

	@Override
	protected void onJoinNetwork() {
		getHandler().refreshPortal(getWorld(), getPos());
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

	private VoidPortalNetworkHandler getHandler() {
		return VoidwayMod.VOID_PORTAL_NETWORK_HANDLER;
	}

}

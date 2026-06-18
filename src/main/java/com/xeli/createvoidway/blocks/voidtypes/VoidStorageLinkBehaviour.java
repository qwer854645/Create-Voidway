package com.xeli.createvoidway.blocks.voidtypes;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

public class VoidStorageLinkBehaviour extends VoidLinkBehaviour {

	public VoidStorageLinkBehaviour(SmartBlockEntity te,
									  Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots) {
		super(te, slots);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().isClientSide)
			return;
		getHandler().addToNetwork(getWorld(), this);
	}

	@Override
	public void unload() {
		if (!getWorld().isClientSide)
			getHandler().removeFromNetwork(getWorld(), this);
		super.unload();
	}

	public Set<BlockPos> getNetwork() {
		return getHandler().getNetworkOf(getWorld(), this);
	}

	@Override
	protected void onJoinNetwork() {
		getHandler().addToNetwork(getWorld(), this);
	}

	@Override
	protected void onLeaveNetwork() {
		getHandler().removeFromNetwork(getWorld(), this);
	}

	public boolean isAlive() {
		Level level = getWorld();
		BlockPos pos = getPos();
		if (blockEntity.isRemoved())
			return false;
		if (!level.isLoaded(pos))
			return false;
		return level.getBlockEntity(pos) == blockEntity;
	}

	private VoidStorageNetworkHandler getHandler() {
		return VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER;
	}

}

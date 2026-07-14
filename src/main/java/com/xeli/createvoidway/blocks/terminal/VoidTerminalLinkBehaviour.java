package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import org.apache.commons.lang3.tuple.Triple;

public class VoidTerminalLinkBehaviour extends VoidLinkBehaviour {

	public VoidTerminalLinkBehaviour(SmartBlockEntity te,
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
		// Keep terminals indexed while their chunk is unloaded so dedicated servers can still
		// list/teleport to distant nodes. Permanent removal happens in destroy()/onLeaveNetwork().
		super.unload();
	}

	@Override
	public void destroy() {
		if (!getWorld().isClientSide)
			getHandler().removeFromNetwork(getWorld(), this);
		super.destroy();
	}

	@Override
	protected void onJoinNetwork() {
		getHandler().addToNetwork(getWorld(), this);
	}

	@Override
	protected void onLeaveNetwork() {
		getHandler().removeFromNetwork(getWorld(), this);
	}

	private VoidTerminalNetworkHandler getHandler() {
		return VoidwayMod.VOID_TERMINAL_NETWORK_HANDLER;
	}

}

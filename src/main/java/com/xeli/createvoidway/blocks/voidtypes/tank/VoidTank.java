package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.simibubi.create.infrastructure.config.AllConfigs;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.networking.packets.VoidTankUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class VoidTank extends FluidTank {

	private static final int CAPACITY = computeCapacity();

	private final NetworkKey key;

	public VoidTank(NetworkKey key) {
		super(CAPACITY);
		this.key = key;
	}

	public int getCapacity() {
		return CAPACITY;
	}

	private static int computeCapacity() {
		return AllConfigs.server().fluids.fluidTankCapacity.get() * 1000;
	}

	@Override
	protected void onContentsChanged() {
		if (VoidwayMod.VOID_TANKS_DATA != null)
			VoidwayMod.VOID_TANKS_DATA.setDirty();
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null)
			PacketDistributor.sendToAllPlayers(new VoidTankUpdatePacket(key, this, server.registryAccess()));
	}

}

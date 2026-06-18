package com.xeli.createvoidway.blocks.voidtypes.chest;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.VoidwayClient;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.networking.packets.VoidChestUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Single shared item storage per void chest channel ({@link NetworkKey}).
 * All input/output chests with the same owner and frequencies use the same instance.
 */
public final class VoidChestStorage {

	private VoidChestStorage() {}

	public static VoidChestInventory of(VoidLinkBehaviour link, boolean clientSide) {
		return of(link.getNetworkKey(), clientSide);
	}

	public static VoidChestInventory of(NetworkKey key, boolean clientSide) {
		if (clientSide)
			return VoidwayClient.VOID_CHESTS.computeStorageIfAbsent(key);
		return VoidwayMod.VOID_CHEST_INVENTORIES_DATA.computeStorageIfAbsent(key);
	}

	public static void onChanged(VoidChestInventory inventory) {
		if (VoidwayMod.VOID_CHEST_INVENTORIES_DATA == null)
			return;

		VoidwayMod.VOID_CHEST_INVENTORIES_DATA.setDirty();
		var server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return;

		PacketDistributor.sendToAllPlayers(
				new VoidChestUpdatePacket(inventory.getKey(), inventory, server.registryAccess()));
	}

}

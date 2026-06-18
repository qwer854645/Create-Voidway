package com.xeli.createvoidway;

import com.xeli.createvoidway.blocks.RWPartialsModels;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageClient;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBattery;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInventory;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTank;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.fluids.VoidTransferFluidClientExtensions;
import com.xeli.createvoidway.ponder.RWPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;

public class VoidwayClient {

	public static final VoidStorageClient<VoidTank> VOID_TANKS = new VoidStorageClient<>(VoidTank::new);

	public static final VoidStorageClient<VoidBattery> VOID_BATTERIES = new VoidStorageClient<>(
			VoidBattery::new);

	public static final VoidStorageClient<VoidChestInventory> VOID_CHESTS = new VoidStorageClient<>(
			VoidChestInventory::new);

	public static void onCtorClient(IEventBus modEventBus) {
		modEventBus.addListener(VoidwayClient::clientInit);
		modEventBus.addListener(VoidwayClient::registerClientExtensions);
		RWPartialsModels.init();
	}

	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		FluidType type = RWFluids.VOID_TRANSFER_FLUID.get().getFluidType();
		event.registerFluidType(VoidTransferFluidClientExtensions.INSTANCE, type);
	}

	public static void clientInit(final FMLClientSetupEvent event) {
		PonderIndex.addPlugin(new RWPonderPlugin());
	}

}

package com.xeli.createvoidway.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.fluids.RWFluids;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class RWPonders {

	public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

		PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> BLOCK = helper.withKeyFunction(RegistryEntry::getId);

		BLOCK.addStoryBoard(RWBlocks.VOID_MOTOR_OUTPUT, "void_motor", VoidScenes::voidMotor);
		BLOCK.addStoryBoard(RWBlocks.VOID_MOTOR_INPUT, "void_motor", VoidScenes::voidMotor);
		BLOCK.addStoryBoard(RWBlocks.VOID_MOTOR_INPUT, "void_motor_io", VoidIoScenes::voidMotorIo);
		BLOCK.addStoryBoard(RWBlocks.VOID_MOTOR_OUTPUT, "void_motor_io", VoidIoScenes::voidMotorIo);

		BLOCK.addStoryBoard(RWBlocks.VOID_CHEST_OUTPUT, "void_chest", VoidScenes::voidChest);
		BLOCK.addStoryBoard(RWBlocks.VOID_CHEST_INPUT, "void_chest", VoidScenes::voidChest);
		BLOCK.addStoryBoard(RWBlocks.VOID_CHEST_INPUT, "void_chest_io", VoidIoScenes::voidChestIo);
		BLOCK.addStoryBoard(RWBlocks.VOID_CHEST_OUTPUT, "void_chest_io", VoidIoScenes::voidChestIo);

		BLOCK.addStoryBoard(RWBlocks.VOID_TANK_OUTPUT, "void_tank", VoidScenes::voidTank);
		BLOCK.addStoryBoard(RWBlocks.VOID_TANK_INPUT, "void_tank", VoidScenes::voidTank);
		BLOCK.addStoryBoard(RWBlocks.VOID_TANK_INPUT, "void_tank_io", VoidIoScenes::voidTankIo);
		BLOCK.addStoryBoard(RWBlocks.VOID_TANK_OUTPUT, "void_tank_io", VoidIoScenes::voidTankIo);

		BLOCK.addStoryBoard(RWBlocks.VOID_BATTERY_OUTPUT, "void_battery", VoidScenes::voidBattery);
		BLOCK.addStoryBoard(RWBlocks.VOID_BATTERY_INPUT, "void_battery", VoidScenes::voidBattery);
		BLOCK.addStoryBoard(RWBlocks.VOID_BATTERY_INPUT, "void_battery_io", VoidIoScenes::voidBatteryIo);
		BLOCK.addStoryBoard(RWBlocks.VOID_BATTERY_OUTPUT, "void_battery_io", VoidIoScenes::voidBatteryIo);

		BLOCK.addStoryBoard(RWBlocks.VOID_TELEPORT_LINK, "void_teleport", TeleportScenes::voidTeleport);
		BLOCK.addStoryBoard(RWBlocks.VOID_TELEPORT_PAD, "void_teleport", TeleportScenes::voidTeleport);

		BLOCK.addStoryBoard(RWBlocks.VOID_NODE_TERMINAL, "void_node_terminal", TerminalScenes::voidNodeTerminal);

		BLOCK.addStoryBoard(RWBlocks.VOID_PORTAL_CONNECTOR, "void_portal", PortalScenes::voidPortal);
		BLOCK.addStoryBoard(RWBlocks.VOID_PORTAL_FRAME, "void_portal", PortalScenes::voidPortal);
		BLOCK.addStoryBoard(RWBlocks.VOID_PORTAL_FLUID, "void_portal", PortalScenes::voidPortal);
		BLOCK.addStoryBoard(RWBlocks.VOID_PORTAL_STRESS, "void_portal", PortalScenes::voidPortal);

		BLOCK.addStoryBoard(RWBlocks.LSHAPED_GEARBOX, "lshaped_gearbox", GearboxScenes::lShapedGearbox);

		helper.withKeyFunction(BuiltInRegistries.ITEM::getKey)
				.forComponents(RWFluids.getBucketStack().getItem())
				.addStoryBoard("void_transfer_fluid", VoidScenes::voidTransferFluid);

	}

}

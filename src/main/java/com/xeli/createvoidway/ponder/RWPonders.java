package com.xeli.createvoidway.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.xeli.createvoidway.blocks.RWBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class RWPonders {

	public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

		PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

		HELPER.addStoryBoard(RWBlocks.VOID_MOTOR_OUTPUT, "void_motor", VoidScenes::voidMotor);
		HELPER.addStoryBoard(RWBlocks.VOID_CHEST_OUTPUT, "void_chest", VoidScenes::voidChest);
		HELPER.addStoryBoard(RWBlocks.VOID_TANK_OUTPUT, "void_tank", VoidScenes::voidTank);
		HELPER.addStoryBoard(RWBlocks.VOID_BATTERY_OUTPUT, "void_battery", VoidScenes::voidBattery);

		HELPER.addStoryBoard(RWBlocks.GEARCUBE, "gearcube", GearboxScenes::gearCube);
		HELPER.addStoryBoard(RWBlocks.LSHAPED_GEARBOX, "lshaped_gearbox", GearboxScenes::lShapedGearbox);

	}

}

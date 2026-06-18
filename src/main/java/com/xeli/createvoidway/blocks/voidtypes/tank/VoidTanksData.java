package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.xeli.createvoidway.blocks.voidtypes.VoidStorageData;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

public class VoidTanksData extends VoidStorageData<VoidTank> {

	public VoidTank computeStorageIfAbsent(NetworkKey key) {
		return super.computeStorageIfAbsent(key, VoidTank::new);
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
		return super.save(tag, registries, VoidTank::isEmpty,
				(provider, tank) -> tank.writeToNBT(provider, new CompoundTag()));
	}

	public static VoidTanksData load(CompoundTag tag, HolderLookup.Provider registries) {
		return load(tag, registries, VoidTanksData::new, VoidTank::new,
				(provider, tank, tankTag) -> tank.readFromNBT(provider, tankTag));
	}

}

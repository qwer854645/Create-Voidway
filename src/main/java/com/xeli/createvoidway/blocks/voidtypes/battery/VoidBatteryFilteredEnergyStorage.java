package com.xeli.createvoidway.blocks.voidtypes.battery;

import net.neoforged.neoforge.energy.IEnergyStorage;

public class VoidBatteryFilteredEnergyStorage implements IEnergyStorage {

	public enum Mode {
		INSERT_ONLY,
		EXTRACT_ONLY,
		BLOCKED
	}

	private final IEnergyStorage delegate;
	private final Mode mode;

	public VoidBatteryFilteredEnergyStorage(IEnergyStorage delegate, Mode mode) {
		this.delegate = delegate;
		this.mode = mode;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (mode != Mode.INSERT_ONLY)
			return 0;
		return delegate.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (mode != Mode.EXTRACT_ONLY)
			return 0;
		return delegate.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored() {
		return delegate.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored() {
		return delegate.getMaxEnergyStored();
	}

	@Override
	public boolean canExtract() {
		return mode == Mode.EXTRACT_ONLY && delegate.canExtract();
	}

	@Override
	public boolean canReceive() {
		return mode == Mode.INSERT_ONLY && delegate.canReceive();
	}

}

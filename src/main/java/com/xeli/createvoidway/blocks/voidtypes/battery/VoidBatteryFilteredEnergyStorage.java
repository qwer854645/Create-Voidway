package com.xeli.createvoidway.blocks.voidtypes.battery;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public class VoidBatteryFilteredEnergyStorage implements IEnergyStorage {

	public enum Mode {
		INSERT_ONLY,
		EXTRACT_ONLY,
		BLOCKED
	}

	private final IEnergyStorage delegate;
	private final Mode mode;
	private final DoubleSupplier transferLossFraction;
	@Nullable
	private final BooleanSupplier canOperate;

	public VoidBatteryFilteredEnergyStorage(IEnergyStorage delegate, Mode mode) {
		this(delegate, mode, () -> 0f, null);
	}

	public VoidBatteryFilteredEnergyStorage(IEnergyStorage delegate, Mode mode, float transferLossFraction) {
		this(delegate, mode, () -> transferLossFraction, null);
	}

	public VoidBatteryFilteredEnergyStorage(IEnergyStorage delegate, Mode mode,
			DoubleSupplier transferLossFraction, @Nullable BooleanSupplier canOperate) {
		this.delegate = delegate;
		this.mode = mode;
		this.transferLossFraction = transferLossFraction;
		this.canOperate = canOperate;
	}

	private boolean isOperable() {
		return canOperate == null || canOperate.getAsBoolean();
	}

	private float lossFraction() {
		return (float) Math.clamp(transferLossFraction.getAsDouble(), 0f, 1f);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (mode != Mode.INSERT_ONLY || !isOperable())
			return 0;
		int stored = delegate.receiveEnergy(applyInsertLoss(maxReceive), simulate);
		return Math.min(reverseInsertLoss(stored), maxReceive);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (mode != Mode.EXTRACT_ONLY || !isOperable())
			return 0;
		float loss = lossFraction();
		if (loss <= 0f)
			return delegate.extractEnergy(maxExtract, simulate);
		int requestedFromChannel = reverseExtractLoss(maxExtract);
		int extracted = delegate.extractEnergy(requestedFromChannel, simulate);
		return Math.min(applyExtractLoss(extracted), maxExtract);
	}

	private int applyInsertLoss(int amount) {
		float loss = lossFraction();
		if (loss <= 0f || amount <= 0)
			return amount;
		return (int) (amount * (1f - loss));
	}

	private int reverseInsertLoss(int stored) {
		float loss = lossFraction();
		if (loss <= 0f || stored <= 0)
			return stored;
		float efficiency = 1f - loss;
		if (efficiency <= 0f)
			return 0;
		return Math.min((int) Math.ceil(stored / efficiency), Integer.MAX_VALUE);
	}

	private int applyExtractLoss(int amount) {
		float loss = lossFraction();
		if (loss <= 0f || amount <= 0)
			return amount;
		return (int) (amount * (1f - loss));
	}

	private int reverseExtractLoss(int desiredDelivery) {
		float loss = lossFraction();
		if (loss <= 0f || desiredDelivery <= 0)
			return desiredDelivery;
		float efficiency = 1f - loss;
		if (efficiency <= 0f)
			return 0;
		return Math.min((int) Math.ceil(desiredDelivery / efficiency), Integer.MAX_VALUE);
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
		return isOperable() && mode == Mode.EXTRACT_ONLY && delegate.canExtract();
	}

	@Override
	public boolean canReceive() {
		return isOperable() && mode == Mode.INSERT_ONLY && delegate.canReceive();
	}

}

package com.xeli.createvoidway.blocks.voidtypes.tank;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class VoidTankFilteredFluidHandler implements IFluidHandler {

	public enum Mode {
		INSERT_ONLY,
		EXTRACT_ONLY,
		BLOCKED
	}

	private final IFluidHandler delegate;
	private final Mode mode;

	public VoidTankFilteredFluidHandler(IFluidHandler delegate, Mode mode) {
		this.delegate = delegate;
		this.mode = mode;
	}

	@Override
	public int getTanks() {
		return delegate.getTanks();
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return delegate.getFluidInTank(tank);
	}

	@Override
	public int getTankCapacity(int tank) {
		return delegate.getTankCapacity(tank);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return delegate.isFluidValid(tank, stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (mode != Mode.INSERT_ONLY)
			return 0;
		return delegate.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (mode != Mode.EXTRACT_ONLY)
			return FluidStack.EMPTY;
		return delegate.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (mode != Mode.EXTRACT_ONLY)
			return FluidStack.EMPTY;
		return delegate.drain(maxDrain, action);
	}

}

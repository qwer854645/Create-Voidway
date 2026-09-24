package com.xeli.createvoidway.blocks.voidtypes.tank;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class VoidTankFilteredFluidHandler implements IFluidHandler {

	public enum Mode {
		INSERT_ONLY,
		EXTRACT_ONLY,
		BLOCKED
	}

	private final IFluidHandler delegate;
	private final Mode mode;
	@Nullable
	private final BooleanSupplier canOperate;

	public VoidTankFilteredFluidHandler(IFluidHandler delegate, Mode mode) {
		this(delegate, mode, null);
	}

	public VoidTankFilteredFluidHandler(IFluidHandler delegate, Mode mode, @Nullable BooleanSupplier canOperate) {
		this.delegate = delegate;
		this.mode = mode;
		this.canOperate = canOperate;
	}

	private boolean isOperable() {
		return canOperate == null || canOperate.getAsBoolean();
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
		return isOperable() && mode == Mode.INSERT_ONLY && delegate.isFluidValid(tank, stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (mode != Mode.INSERT_ONLY || !isOperable())
			return 0;
		return delegate.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (mode != Mode.EXTRACT_ONLY || !isOperable())
			return FluidStack.EMPTY;
		return delegate.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (mode != Mode.EXTRACT_ONLY || !isOperable())
			return FluidStack.EMPTY;
		return delegate.drain(maxDrain, action);
	}

}

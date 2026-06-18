package com.xeli.createvoidway.fluids;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Accepts only void transfer fluid; rejects all other fluids on fill, load, and direct set.
 */
public class VoidTransferFluidTank extends FluidTank {

	private final Runnable onChanged;

	public VoidTransferFluidTank(int capacity, Runnable onChanged) {
		super(capacity, RWFluids::isAllowedInVoidMotorInput);
		this.onChanged = onChanged;
	}

	@Override
	protected void onContentsChanged() {
		super.onContentsChanged();
		onChanged.run();
	}

	@Override
	public boolean isFluidValid(FluidStack stack) {
		return RWFluids.isAllowedInVoidMotorInput(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (!RWFluids.isAllowedInVoidMotorInput(resource))
			return 0;
		return super.fill(resource, action);
	}

	@Override
	public void setFluid(FluidStack stack) {
		if (!stack.isEmpty() && !RWFluids.isAllowedInVoidMotorInput(stack))
			stack = FluidStack.EMPTY;
		super.setFluid(stack);
	}

	public void purgeInvalidContents() {
		FluidStack current = getFluid();
		if (!current.isEmpty() && !RWFluids.isAllowedInVoidMotorInput(current))
			setFluid(FluidStack.EMPTY);
	}

}

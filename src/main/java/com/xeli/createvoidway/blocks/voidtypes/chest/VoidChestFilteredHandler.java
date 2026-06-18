package com.xeli.createvoidway.blocks.voidtypes.chest;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class VoidChestFilteredHandler implements IItemHandlerModifiable {

	public enum Mode {
		INSERT_ONLY,
		EXTRACT_ONLY,
		BLOCKED
	}

	private final IItemHandlerModifiable delegate;
	private final Mode mode;

	public VoidChestFilteredHandler(VoidChestInventory inventory, Mode mode) {
		this.delegate = inventory;
		this.mode = mode;
	}

	@Override
	public int getSlots() {
		return delegate.getSlots();
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		return delegate.getStackInSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		delegate.setStackInSlot(slot, stack);
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (mode != Mode.INSERT_ONLY)
			return stack;
		return delegate.insertItem(slot, stack, simulate);
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (mode != Mode.EXTRACT_ONLY)
			return ItemStack.EMPTY;
		return delegate.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return delegate.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return delegate.isItemValid(slot, stack);
	}

}

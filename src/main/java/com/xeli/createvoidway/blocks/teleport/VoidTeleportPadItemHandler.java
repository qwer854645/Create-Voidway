package com.xeli.createvoidway.blocks.teleport;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class VoidTeleportPadItemHandler implements IItemHandler {

	private final VoidTeleportPadTileEntity pad;

	public VoidTeleportPadItemHandler(VoidTeleportPadTileEntity pad) {
		this.pad = pad;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		return pad.tryInsertItemForTeleport(stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return !stack.isEmpty();
	}

	public static boolean isInsertSide(Direction side) {
		return side == Direction.UP;
	}

}

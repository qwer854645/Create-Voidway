package com.xeli.createvoidway.blocks.voidtypes.chest;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import com.xeli.createvoidway.blocks.voidtypes.RWContainerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class VoidChestContainer extends MenuBase<AbstractVoidChestTileEntity> {

	public VoidChestContainer(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public VoidChestContainer(MenuType<?> type, int id, Inventory inv, AbstractVoidChestTileEntity te) {
		super(type, id, inv, te);
		contentHolder.startOpen(player);
	}

	public static VoidChestContainer create(int id, Inventory inv, AbstractVoidChestTileEntity te) {
		return new VoidChestContainer(RWContainerTypes.VOID_CHEST.get(), id, inv, te);
	}

	@Override
	protected AbstractVoidChestTileEntity createOnClient(RegistryFriendlyByteBuf extraData) {
		BlockPos readBlockPos = extraData.readBlockPos();
		CompoundTag readNbt = extraData.readNbt();

		ClientLevel world = Minecraft.getInstance().level;
		assert world != null;
		BlockEntity tileEntity = world.getBlockEntity(readBlockPos);
		if (tileEntity instanceof AbstractVoidChestTileEntity voidChest) {
			voidChest.read(readNbt, world.registryAccess(), true);
			return voidChest;
		}

		return null;
	}

	@Override
	protected void initAndReadInventory(AbstractVoidChestTileEntity contentHolder) {}

	@Override
	protected void addSlots() {
		addChestSlots();
		addPlayerSlots(8, 90);
	}

	private void addChestSlots() {
		VoidChestInventory inventory = contentHolder.getItemStorage();
		boolean input = contentHolder.isVoidChestInput();
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				int slot = y * 9 + x;
				addSlot(new SlotItemHandler(inventory, slot, 8 + x * 18, 18 + y * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return input && super.mayPlace(stack);
					}

					@Override
					public boolean mayPickup(Player player) {
						return !input && super.mayPickup(player);
					}
				});
			}
		}
	}

	@Override
	protected void saveData(AbstractVoidChestTileEntity contentHolder) {}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int size = contentHolder.getItemStorage().getSlots();
		boolean success;
		if (index < size) {
			success = !moveItemStackTo(stack, size, slots.size(), false);
			contentHolder.getItemStorage().onContentsChanged(index);
		} else
			success = !moveItemStackTo(stack, 0, size - 1, false);

		return success ? ItemStack.EMPTY : stack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!player.level().isClientSide)
			contentHolder.stopOpen(player);
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(player) && contentHolder.canOperate();
	}

	public boolean isDisplayingNetwork(NetworkKey key) {
		return contentHolder.getItemStorage().getKey().equals(key);
	}

	public void onStorageUpdated() {
		broadcastChanges();
	}

}

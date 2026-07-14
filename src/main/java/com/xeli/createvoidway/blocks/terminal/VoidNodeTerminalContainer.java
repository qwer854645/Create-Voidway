package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import com.xeli.createvoidway.blocks.voidtypes.RWContainerTypes;
import com.xeli.createvoidway.networking.packets.VoidNodePlayerListPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoidNodeTerminalContainer extends MenuBase<VoidNodeTerminalTileEntity> {

	private final List<VoidNodeEntry> nodes = new ArrayList<>();
	private final List<VoidTerminalPlayerEntry> players = new ArrayList<>();
	private int selectedIndex = -1;
	private int selectedPlayerIndex = -1;
	private boolean hasDeathLocation;
	private int deathDistanceBlocks = VoidTerminalPlayerEntry.DISTANCE_OTHER_DIMENSION;

	public VoidNodeTerminalContainer(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
		com.xeli.createvoidway.networking.packets.VoidNodeListPacket.applyPending(this);
		VoidNodePlayerListPacket.applyPending(this);
	}

	public VoidNodeTerminalContainer(MenuType<?> type, int id, Inventory inv, VoidNodeTerminalTileEntity te) {
		super(type, id, inv, te);
		contentHolder.startOpen(player);
	}

	public static VoidNodeTerminalContainer create(int id, Inventory inv, VoidNodeTerminalTileEntity te) {
		return new VoidNodeTerminalContainer(RWContainerTypes.VOID_NODE_TERMINAL.get(), id, inv, te);
	}

	@Override
	protected VoidNodeTerminalTileEntity createOnClient(RegistryFriendlyByteBuf extraData) {
		BlockPos readBlockPos = extraData.readBlockPos();
		CompoundTag readNbt = extraData.readNbt();
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null)
			return null;
		BlockEntity tileEntity = world.getBlockEntity(readBlockPos);
		if (tileEntity instanceof VoidNodeTerminalTileEntity terminal) {
			terminal.read(readNbt, world.registryAccess(), true);
			return terminal;
		}
		return null;
	}

	@Override
	protected void initAndReadInventory(VoidNodeTerminalTileEntity contentHolder) {
	}

	@Override
	protected void addSlots() {
	}

	@Override
	protected void saveData(VoidNodeTerminalTileEntity contentHolder) {
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!player.level().isClientSide)
			contentHolder.stopOpen(player);
	}

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(player);
	}

	public BlockPos getTerminalPos() {
		return contentHolder.getBlockPos();
	}

	public boolean matchesTerminal(BlockPos terminalPos) {
		return contentHolder.getBlockPos().equals(terminalPos);
	}

	public void updateNodes(List<VoidNodeEntry> entries) {
		nodes.clear();
		nodes.addAll(entries);
		if (selectedIndex >= nodes.size())
			selectedIndex = nodes.isEmpty() ? -1 : 0;
		else if (selectedIndex < 0 && !nodes.isEmpty())
			selectedIndex = 0;
	}

	public List<VoidNodeEntry> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int index) {
		if (index >= 0 && index < nodes.size())
			selectedIndex = index;
	}

	public VoidNodeEntry getSelectedNode() {
		if (selectedIndex < 0 || selectedIndex >= nodes.size())
			return null;
		return nodes.get(selectedIndex);
	}

	public void updatePlayers(boolean hasDeathLocation, int deathDistanceBlocks, List<VoidTerminalPlayerEntry> entries) {
		this.hasDeathLocation = hasDeathLocation;
		this.deathDistanceBlocks = deathDistanceBlocks;
		players.clear();
		players.addAll(entries);
		if (selectedPlayerIndex >= players.size())
			selectedPlayerIndex = players.isEmpty() ? -1 : 0;
		else if (selectedPlayerIndex < 0 && !players.isEmpty())
			selectedPlayerIndex = 0;
	}

	public List<VoidTerminalPlayerEntry> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public boolean hasDeathLocation() {
		return hasDeathLocation;
	}

	public int getDeathDistanceBlocks() {
		return deathDistanceBlocks;
	}

	public int getSelectedPlayerIndex() {
		return selectedPlayerIndex;
	}

	public void setSelectedPlayerIndex(int index) {
		if (index >= 0 && index < players.size())
			selectedPlayerIndex = index;
	}

	public VoidTerminalPlayerEntry getSelectedPlayer() {
		if (selectedPlayerIndex < 0 || selectedPlayerIndex >= players.size())
			return null;
		return players.get(selectedPlayerIndex);
	}

}

package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.xeli.createvoidway.networking.packets.VoidNodeRenamePacket;
import com.xeli.createvoidway.networking.packets.VoidNodeRequestListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeRequestPlayerListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeTeleportDeathPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeTeleportPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeTeleportPlayerPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class VoidNodeTerminalScreen extends AbstractSimiContainerScreen<VoidNodeTerminalContainer> {

	private static final int ROWS = 6;
	private static final int ROW_HEIGHT = 18;
	private static final int LIST_TOP = 36;
	private static final int RENAME_TOP = 148;
	private static final int ACTION_TOP = 168;

	private enum Tab {
		NODES,
		PLAYERS
	}

	private EditBox nameField;
	private Button renameButton;
	private Button teleportButton;
	private Button teleportDeathButton;
	private Button scrollUpButton;
	private Button scrollDownButton;
	private final List<NodeRowWidget> nodeRowWidgets = new ArrayList<>();
	private final List<PlayerRowWidget> playerRowWidgets = new ArrayList<>();
	private int scrollOffset;
	private Tab activeTab = Tab.NODES;
	private boolean nodeListRequested;
	private boolean playerListRequested;

	public VoidNodeTerminalScreen(VoidNodeTerminalContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		this.imageWidth = 220;
		this.imageHeight = 186;
	}

	public void onNodesUpdated() {
		if (activeTab != Tab.NODES)
			return;
		scrollOffset = Math.min(scrollOffset, Math.max(0, menu.getNodes().size() - ROWS));
		syncNameFieldFromSelection();
		rebuildScrollButtons();
		rebuildNodeRows();
	}

	public void onPlayersUpdated() {
		if (activeTab != Tab.PLAYERS)
			return;
		scrollOffset = Math.min(scrollOffset, Math.max(0, menu.getPlayers().size() - ROWS));
		rebuildScrollButtons();
		rebuildPlayerRows();
		updateDeathButtonState();
	}

	@Override
	protected void init() {
		super.init();
		requestActiveTabData();

		addRenderableWidget(Button.builder(Component.translatable("createvoidway.void_node_terminal.tab.nodes"),
				b -> switchTab(Tab.NODES)).bounds(leftPos + 8, topPos + 16, 50, 18).build());
		addRenderableWidget(Button.builder(Component.translatable("createvoidway.void_node_terminal.tab.players"),
				b -> switchTab(Tab.PLAYERS)).bounds(leftPos + 62, topPos + 16, 50, 18).build());

		nameField = new EditBox(font, leftPos + 8, topPos + RENAME_TOP, 140, 18, Component.literal(""));
		nameField.setMaxLength(32);
		nameField.setHint(Component.translatable("createvoidway.void_node_terminal.rename_hint"));
		addRenderableWidget(nameField);

		renameButton = addRenderableWidget(Button.builder(Component.translatable("createvoidway.void_node_terminal.rename"),
				b -> sendRename()).bounds(leftPos + 152, topPos + RENAME_TOP, 60, 18).build());

		teleportButton = addRenderableWidget(Button.builder(Component.translatable("createvoidway.void_node_terminal.teleport"),
				b -> sendTeleport()).bounds(leftPos + 8, topPos + ACTION_TOP, 100, 18).build());

		teleportDeathButton = addRenderableWidget(Button.builder(
				Component.translatable("createvoidway.void_node_terminal.teleport_death"),
				b -> sendTeleportDeath()).bounds(leftPos + 112, topPos + ACTION_TOP, 100, 18).build());

		rebuildScrollButtons();
		rebuildNodeRows();
		rebuildPlayerRows();
		syncNameFieldFromSelection();
		updateTabVisibility();
		updateDeathButtonState();
	}

	private void updateDeathButtonState() {
		if (teleportDeathButton != null)
			teleportDeathButton.active = activeTab == Tab.PLAYERS && menu.hasDeathLocation();
	}

	private void switchTab(Tab tab) {
		if (activeTab == tab)
			return;
		activeTab = tab;
		scrollOffset = 0;
		requestActiveTabData();
		rebuildScrollButtons();
		rebuildNodeRows();
		rebuildPlayerRows();
		syncNameFieldFromSelection();
		updateTabVisibility();
		updateDeathButtonState();
	}

	private void requestActiveTabData() {
		BlockPos terminalPos = menu.getTerminalPos();
		if (activeTab == Tab.NODES) {
			if (!nodeListRequested) {
				PacketDistributor.sendToServer(new VoidNodeRequestListPacket(terminalPos));
				nodeListRequested = true;
			}
			return;
		}
		if (!playerListRequested) {
			PacketDistributor.sendToServer(new VoidNodeRequestPlayerListPacket(terminalPos));
			playerListRequested = true;
		}
	}

	private void updateTabVisibility() {
		boolean nodesTab = activeTab == Tab.NODES;
		if (nameField != null)
			nameField.visible = nodesTab;
		if (renameButton != null)
			renameButton.visible = nodesTab;
		if (teleportDeathButton != null)
			teleportDeathButton.visible = !nodesTab;
		for (NodeRowWidget widget : nodeRowWidgets)
			widget.visible = nodesTab;
		for (PlayerRowWidget widget : playerRowWidgets)
			widget.visible = !nodesTab;
	}

	private void rebuildScrollButtons() {
		if (scrollUpButton != null) {
			removeWidget(scrollUpButton);
			scrollUpButton = null;
		}
		if (scrollDownButton != null) {
			removeWidget(scrollDownButton);
			scrollDownButton = null;
		}

		int size = activeTab == Tab.NODES ? menu.getNodes().size() : menu.getPlayers().size();
		int scrollTop = topPos + LIST_TOP - 2;

		if (scrollOffset > 0)
			scrollUpButton = addRenderableWidget(Button.builder(Component.literal("\u25B2"), b -> scroll(-1))
					.bounds(leftPos + 200, scrollTop, 12, 12).build());
		if (scrollOffset + ROWS < size)
			scrollDownButton = addRenderableWidget(Button.builder(Component.literal("\u25BC"), b -> scroll(1))
					.bounds(leftPos + 200, scrollTop + ROWS * ROW_HEIGHT, 12, 12).build());
	}

	private void rebuildNodeRows() {
		for (NodeRowWidget widget : nodeRowWidgets)
			removeWidget(widget);
		nodeRowWidgets.clear();

		List<VoidNodeEntry> nodes = menu.getNodes();
		for (int row = 0; row < ROWS; row++) {
			int index = scrollOffset + row;
			if (index >= nodes.size())
				break;
			NodeRowWidget widget = new NodeRowWidget(leftPos + 8, topPos + LIST_TOP + row * ROW_HEIGHT, 188, 16, index);
			widget.visible = activeTab == Tab.NODES;
			nodeRowWidgets.add(widget);
			addRenderableWidget(widget);
		}
	}

	private void rebuildPlayerRows() {
		for (PlayerRowWidget widget : playerRowWidgets)
			removeWidget(widget);
		playerRowWidgets.clear();

		List<VoidTerminalPlayerEntry> players = menu.getPlayers();
		for (int row = 0; row < ROWS; row++) {
			int index = scrollOffset + row;
			if (index >= players.size())
				break;
			PlayerRowWidget widget = new PlayerRowWidget(leftPos + 8, topPos + LIST_TOP + row * ROW_HEIGHT, 188, 16,
					index);
			widget.visible = activeTab == Tab.PLAYERS;
			playerRowWidgets.add(widget);
			addRenderableWidget(widget);
		}
	}

	private void scroll(int delta) {
		int size = activeTab == Tab.NODES ? menu.getNodes().size() : menu.getPlayers().size();
		scrollOffset = Math.max(0, Math.min(scrollOffset + delta, Math.max(0, size - ROWS)));
		rebuildScrollButtons();
		if (activeTab == Tab.NODES)
			rebuildNodeRows();
		else
			rebuildPlayerRows();
	}

	private void selectNode(int index) {
		menu.setSelectedIndex(index);
		syncNameFieldFromSelection();
		rebuildNodeRows();
	}

	private void selectPlayer(int index) {
		menu.setSelectedPlayerIndex(index);
		rebuildPlayerRows();
	}

	private void syncNameFieldFromSelection() {
		if (nameField == null)
			return;
		VoidNodeEntry selected = menu.getSelectedNode();
		if (selected != null)
			nameField.setValue(selected.renameName());
	}

	private void sendRename() {
		if (activeTab != Tab.NODES)
			return;
		VoidNodeEntry selected = menu.getSelectedNode();
		if (selected == null)
			return;
		BlockPos terminalPos = menu.getTerminalPos();
		PacketDistributor.sendToServer(new VoidNodeRenamePacket(terminalPos, selected.dimension(), selected.pos(),
				nameField.getValue()));
	}

	private void sendTeleport() {
		BlockPos terminalPos = menu.getTerminalPos();
		if (activeTab == Tab.NODES) {
			VoidNodeEntry selected = menu.getSelectedNode();
			if (selected == null || selected.currentTerminal())
				return;
			PacketDistributor.sendToServer(new VoidNodeTeleportPacket(terminalPos, selected.dimension(), selected.pos()));
			return;
		}

		VoidTerminalPlayerEntry selected = menu.getSelectedPlayer();
		if (selected == null)
			return;
		PacketDistributor.sendToServer(new VoidNodeTeleportPlayerPacket(terminalPos, selected.uuid()));
	}

	private void sendTeleportDeath() {
		if (activeTab != Tab.PLAYERS || !menu.hasDeathLocation())
			return;
		PacketDistributor.sendToServer(new VoidNodeTeleportDeathPacket(menu.getTerminalPos()));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
		graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF202020);
		graphics.drawString(font, title, leftPos + 8, topPos + 8, 0xFFFFFF, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		if (activeTab == Tab.NODES && menu.getNodes().isEmpty())
			graphics.drawString(font, Component.translatable("createvoidway.void_node_terminal.no_nodes"),
					leftPos + 8, topPos + LIST_TOP + 2, 0xAAAAAA, false);
		if (activeTab == Tab.PLAYERS && menu.getPlayers().isEmpty())
			graphics.drawString(font, Component.translatable("createvoidway.void_node_terminal.no_players"),
					leftPos + 8, topPos + LIST_TOP + 2, 0xAAAAAA, false);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
	}

	private final class NodeRowWidget extends AbstractWidget {

		private final int nodeIndex;

		private NodeRowWidget(int x, int y, int width, int height, int nodeIndex) {
			super(x, y, width, height, Component.empty());
			this.nodeIndex = nodeIndex;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			List<VoidNodeEntry> nodes = menu.getNodes();
			if (nodeIndex < 0 || nodeIndex >= nodes.size())
				return;

			VoidNodeEntry entry = nodes.get(nodeIndex);
			int bg = nodeIndex == menu.getSelectedIndex() ? 0xFF3A5F8A : 0xFF2A2A2A;
			graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);
			if (isHovered())
				graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);

			String label = entry.displayName();
			if (entry.currentTerminal())
				label = label + " *";

			String distanceText = entry.distanceText().getString();
			int distanceWidth = font.width(distanceText);
			int maxNameWidth = width - distanceWidth - 12;
			label = font.plainSubstrByWidth(label, Math.max(0, maxNameWidth));

			graphics.drawString(font, label, getX() + 4, getY() + 4, 0xFFFFFF, false);
			graphics.drawString(font, distanceText, getX() + width - distanceWidth - 4, getY() + 4, 0xAAAAAA, false);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			selectNode(nodeIndex);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}

	}

	private final class PlayerRowWidget extends AbstractWidget {

		private final int playerIndex;

		private PlayerRowWidget(int x, int y, int width, int height, int playerIndex) {
			super(x, y, width, height, Component.empty());
			this.playerIndex = playerIndex;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			List<VoidTerminalPlayerEntry> players = menu.getPlayers();
			if (playerIndex < 0 || playerIndex >= players.size())
				return;

			VoidTerminalPlayerEntry entry = players.get(playerIndex);
			int bg = playerIndex == menu.getSelectedPlayerIndex() ? 0xFF3A5F8A : 0xFF2A2A2A;
			graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);
			if (isHovered())
				graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);

			String label = entry.displayName();
			String distanceText = entry.distanceText().getString();
			int distanceWidth = font.width(distanceText);
			int maxNameWidth = width - distanceWidth - 12;
			label = font.plainSubstrByWidth(label, Math.max(0, maxNameWidth));

			graphics.drawString(font, label, getX() + 4, getY() + 4, 0xFFFFFF, false);
			graphics.drawString(font, distanceText, getX() + width - distanceWidth - 4, getY() + 4, 0xAAAAAA, false);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			selectPlayer(playerIndex);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}

	}

}

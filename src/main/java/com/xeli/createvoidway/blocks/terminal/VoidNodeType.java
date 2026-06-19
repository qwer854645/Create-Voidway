package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.blocks.RWBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum VoidNodeType {
	MOTOR_INPUT("void_motor_input"),
	MOTOR_OUTPUT("void_motor_output"),
	CHEST_INPUT("void_chest_input"),
	CHEST_OUTPUT("void_chest_output"),
	TANK_INPUT("void_tank_input"),
	TANK_OUTPUT("void_tank_output"),
	BATTERY_INPUT("void_battery_input"),
	BATTERY_OUTPUT("void_battery_output"),
	TELEPORT_LINK("void_teleport_link"),
	TELEPORT_PAD("void_teleport_pad"),
	PORTAL_CONNECTOR("void_portal_connector"),
	PORTAL_FLUID("void_portal_fluid"),
	PORTAL_STRESS("void_portal_stress"),
	TERMINAL("void_node_terminal"),
	UNKNOWN("void_node_unknown");

	private final String blockKey;

	VoidNodeType(String blockKey) {
		this.blockKey = blockKey;
	}

	public String getBlockKey() {
		return blockKey;
	}

	public static VoidNodeType fromBlockState(BlockState state) {
		Block block = state.getBlock();
		if (block == RWBlocks.VOID_MOTOR_INPUT.get()) return MOTOR_INPUT;
		if (block == RWBlocks.VOID_MOTOR_OUTPUT.get()) return MOTOR_OUTPUT;
		if (block == RWBlocks.VOID_CHEST_INPUT.get()) return CHEST_INPUT;
		if (block == RWBlocks.VOID_CHEST_OUTPUT.get()) return CHEST_OUTPUT;
		if (block == RWBlocks.VOID_TANK_INPUT.get()) return TANK_INPUT;
		if (block == RWBlocks.VOID_TANK_OUTPUT.get()) return TANK_OUTPUT;
		if (block == RWBlocks.VOID_BATTERY_INPUT.get()) return BATTERY_INPUT;
		if (block == RWBlocks.VOID_BATTERY_OUTPUT.get()) return BATTERY_OUTPUT;
		if (block == RWBlocks.VOID_TELEPORT_LINK.get()) return TELEPORT_LINK;
		if (block == RWBlocks.VOID_TELEPORT_PAD.get()) return TELEPORT_PAD;
		if (block == RWBlocks.VOID_PORTAL_CONNECTOR.get()) return PORTAL_CONNECTOR;
		if (block == RWBlocks.VOID_PORTAL_FLUID.get()) return PORTAL_FLUID;
		if (block == RWBlocks.VOID_PORTAL_STRESS.get()) return PORTAL_STRESS;
		if (block == RWBlocks.VOID_NODE_TERMINAL.get()) return TERMINAL;
		if (block == RWBlocks.VOID_NODE_TERMINAL_TOP.get()) return TERMINAL;
		return UNKNOWN;
	}

	public static String defaultNameComponentKey(VoidNodeType type) {
		return "createvoidway.void_node.type." + type.name().toLowerCase();
	}

	public static boolean isTerminalDestination(VoidNodeType type) {
		return type == TERMINAL;
	}

	public static String formatCoords(BlockPos pos) {
		return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
	}

}

package com.xeli.createvoidway.blocks;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.xeli.createvoidway.blocks.gearcube.GearcubeVisual;
import com.xeli.createvoidway.blocks.gearcube.SimpleKineticRenderer;
import com.xeli.createvoidway.blocks.lgearbox.LShapedGearboxVisual;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryInputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryInputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryOutputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryOutputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryVisual;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestOutputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestOutputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestVisual;
import com.xeli.createvoidway.blocks.voidtypes.motor.DirectedVoidMotorRenderer;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorInputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorInputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorOutputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankInputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankInputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankOutputRenderer;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankOutputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankVisual;

import static com.xeli.createvoidway.VoidwayMod.REGISTRATE;

public class RWTileEntities {

	public static final BlockEntityEntry<VoidMotorOutputTileEntity> VOID_MOTOR_OUTPUT = REGISTRATE
			.blockEntity("void_motor_output", VoidMotorOutputTileEntity::new)
			.visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), true)
			.validBlocks(RWBlocks.VOID_MOTOR_OUTPUT)
			.renderer(() -> DirectedVoidMotorRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidMotorInputTileEntity> VOID_MOTOR_INPUT = REGISTRATE
			.blockEntity("void_motor_input", VoidMotorInputTileEntity::new)
			.visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), true)
			.validBlocks(RWBlocks.VOID_MOTOR_INPUT)
			.renderer(() -> VoidMotorInputRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidChestOutputTileEntity> VOID_CHEST_OUTPUT = REGISTRATE
			.blockEntity("void_chest_output", VoidChestOutputTileEntity::new)
			.visual(() -> VoidChestVisual::new, true)
			.validBlocks(RWBlocks.VOID_CHEST_OUTPUT)
			.renderer(() -> VoidChestOutputRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidChestInputTileEntity> VOID_CHEST_INPUT = REGISTRATE
			.blockEntity("void_chest_input", VoidChestInputTileEntity::new)
			.visual(() -> VoidChestVisual::new, true)
			.validBlocks(RWBlocks.VOID_CHEST_INPUT)
			.renderer(() -> VoidChestInputRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidTankOutputTileEntity> VOID_TANK_OUTPUT = REGISTRATE
			.blockEntity("void_tank_output", VoidTankOutputTileEntity::new)
			.visual(() -> VoidTankVisual::new, true)
			.validBlocks(RWBlocks.VOID_TANK_OUTPUT)
			.renderer(() -> VoidTankOutputRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidTankInputTileEntity> VOID_TANK_INPUT = REGISTRATE
			.blockEntity("void_tank_input", VoidTankInputTileEntity::new)
			.visual(() -> VoidTankVisual::new, true)
			.validBlocks(RWBlocks.VOID_TANK_INPUT)
			.renderer(() -> VoidTankInputRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidBatteryOutputTileEntity> VOID_BATTERY_OUTPUT = REGISTRATE
			.blockEntity("void_battery_output", VoidBatteryOutputTileEntity::new)
			.visual(() -> VoidBatteryVisual::new, true)
			.validBlocks(RWBlocks.VOID_BATTERY_OUTPUT)
			.renderer(() -> VoidBatteryOutputRenderer::new)
			.register();

	public static final BlockEntityEntry<VoidBatteryInputTileEntity> VOID_BATTERY_INPUT = REGISTRATE
			.blockEntity("void_battery_input", VoidBatteryInputTileEntity::new)
			.visual(() -> VoidBatteryVisual::new, true)
			.validBlocks(RWBlocks.VOID_BATTERY_INPUT)
			.renderer(() -> VoidBatteryInputRenderer::new)
			.register();

	public static final BlockEntityEntry<GearboxBlockEntity> GEARCUBE = REGISTRATE
			.blockEntity("gearcube", GearboxBlockEntity::new)
			.visual(() -> GearcubeVisual::new, false)
			.validBlocks(RWBlocks.GEARCUBE)
			.renderer(() -> SimpleKineticRenderer::new)
			.register();

	public static final BlockEntityEntry<GearboxBlockEntity> LSHAPED_GEARBOX = REGISTRATE
			.blockEntity("lshaped_gearbox", GearboxBlockEntity::new)
			.visual(() -> LShapedGearboxVisual::new, false)
			.validBlocks(RWBlocks.LSHAPED_GEARBOX)
			.renderer(() -> SimpleKineticRenderer::new)
			.register();

	public static void register() {}

}

package com.xeli.createvoidway.config;

import com.simibubi.create.api.stress.BlockStressValues;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorOutputTileEntity;

public final class VoidwayStress {

	private VoidwayStress() {}

	public static void register() {
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_MOTOR_INPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_CHEST_INPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_CHEST_OUTPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_BATTERY_INPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_BATTERY_OUTPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_TANK_INPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_TANK_OUTPUT.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_TELEPORT_PAD.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_PORTAL_STRESS.get(), () -> 0.0);
		BlockStressValues.IMPACTS.register(RWBlocks.VOID_NODE_TERMINAL.get(), () -> 0.0);
		BlockStressValues.CAPACITIES.register(RWBlocks.VOID_MOTOR_OUTPUT.get(),
				() -> (double) VoidMotorOutputTileEntity.OUTPUT_STRESS_CAPACITY);
		BlockStressValues.RPM.register(RWBlocks.VOID_MOTOR_OUTPUT.get(),
				new BlockStressValues.GeneratedRpm(VoidMotorOutputTileEntity.MAX_SPEED, true));
	}

}

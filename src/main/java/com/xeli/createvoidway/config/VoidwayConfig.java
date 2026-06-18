package com.xeli.createvoidway.config;

import com.xeli.createvoidway.VoidwayMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = VoidwayMod.ID)
public final class VoidwayConfig {

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	private static final ModConfigSpec.IntValue VOID_MOTOR_INPUT_LOCAL_STRESS_PERCENT = BUILDER
			.comment("Percentage of locally received stress consumed by the void motor input.")
			.comment("The remainder is injected into the voidway channel.")
			.defineInRange("voidMotorInputLocalStressPercent", 10, 0, 100);

	private static final ModConfigSpec.IntValue VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_PERCENT = BUILDER
			.comment("Percentage of channel stress contribution consumed as void transfer fluid each tick.")
			.comment("The actual drain is the greater of this value and voidMotorInputTransferFluidDrainMinMbPerTick.")
			.comment("For example, 1 means 500 SU contributed drains 5 mB per tick.")
			.defineInRange("voidMotorInputTransferFluidDrainPercent", 1, 0, 100);

	private static final ModConfigSpec.IntValue VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_MIN_MB = BUILDER
			.comment("Minimum void transfer fluid drained per tick while the input is relaying.")
			.defineInRange("voidMotorInputTransferFluidDrainMinMbPerTick", 5, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_CHEST_STRESS_BASE = BUILDER
			.comment("Stress demand (SU) for void chest input and output when the linked channel is empty.")
			.comment("Demand scales linearly with occupied inventory slots up to voidChestStressAtFullChannel.")
			.defineInRange("voidChestStressBase", 256, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_CHEST_STRESS_AT_FULL_CHANNEL = BUILDER
			.comment("Stress demand (SU) for void chest input and output when all channel slots are occupied.")
			.defineInRange("voidChestStressAtFullChannel", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_CHEST_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void chest is operating.")
			.defineInRange("voidChestTransferFluidDrainMbPerTick", 1, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_BATTERY_STRESS_BASE = BUILDER
			.comment("Stress demand (SU) for void battery input and output when the linked channel is empty.")
			.comment("Demand scales linearly with stored energy up to voidBatteryStressAtFullChannel.")
			.defineInRange("voidBatteryStressBase", 256, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_BATTERY_STRESS_AT_FULL_CHANNEL = BUILDER
			.comment("Stress demand (SU) for void battery input and output when the channel battery is full.")
			.defineInRange("voidBatteryStressAtFullChannel", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_BATTERY_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void battery is operating.")
			.defineInRange("voidBatteryTransferFluidDrainMbPerTick", 1, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_TANK_STRESS_BASE = BUILDER
			.comment("Stress demand (SU) for void tank input and output when the linked channel is empty.")
			.comment("Demand scales linearly with stored fluid up to voidTankStressAtFullChannel.")
			.defineInRange("voidTankStressBase", 256, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TANK_STRESS_AT_FULL_CHANNEL = BUILDER
			.comment("Stress demand (SU) for void tank input and output when the channel tank is full.")
			.defineInRange("voidTankStressAtFullChannel", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TANK_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void tank is operating.")
			.defineInRange("voidTankTransferFluidDrainMbPerTick", 1, 0, 10000);

	public static final ModConfigSpec SPEC = BUILDER.build();

	private static int voidMotorInputLocalStressPercent = VOID_MOTOR_INPUT_LOCAL_STRESS_PERCENT.getDefault();
	private static int voidMotorInputTransferFluidDrainPercent = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_PERCENT.getDefault();
	private static int voidMotorInputTransferFluidDrainMinMbPerTick = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_MIN_MB.getDefault();
	private static int voidChestStressBase = VOID_CHEST_STRESS_BASE.getDefault();
	private static int voidChestStressAtFullChannel = VOID_CHEST_STRESS_AT_FULL_CHANNEL.getDefault();
	private static int voidChestTransferFluidDrainMbPerTick = VOID_CHEST_TRANSFER_FLUID_DRAIN_MB.getDefault();
	private static int voidBatteryStressBase = VOID_BATTERY_STRESS_BASE.getDefault();
	private static int voidBatteryStressAtFullChannel = VOID_BATTERY_STRESS_AT_FULL_CHANNEL.getDefault();
	private static int voidBatteryTransferFluidDrainMbPerTick = VOID_BATTERY_TRANSFER_FLUID_DRAIN_MB.getDefault();
	private static int voidTankStressBase = VOID_TANK_STRESS_BASE.getDefault();
	private static int voidTankStressAtFullChannel = VOID_TANK_STRESS_AT_FULL_CHANNEL.getDefault();
	private static int voidTankTransferFluidDrainMbPerTick = VOID_TANK_TRANSFER_FLUID_DRAIN_MB.getDefault();

	private VoidwayConfig() {}

	public static float getInputLocalStressFraction() {
		return voidMotorInputLocalStressPercent / 100f;
	}

	public static float getInputNetworkContributionFraction() {
		return 1f - getInputLocalStressFraction();
	}

	public static int getInputLocalStressPercent() {
		return voidMotorInputLocalStressPercent;
	}

	public static int getInputTransferFluidDrainPercent() {
		return voidMotorInputTransferFluidDrainPercent;
	}

	public static int getInputTransferFluidDrainMinMbPerTick() {
		return voidMotorInputTransferFluidDrainMinMbPerTick;
	}

	public static int getVoidChestStressBase() {
		return voidChestStressBase;
	}

	public static int getVoidChestStressAtFullChannel() {
		return voidChestStressAtFullChannel;
	}

	public static int getVoidChestTransferFluidDrainMbPerTick() {
		return voidChestTransferFluidDrainMbPerTick;
	}

	public static int getVoidBatteryStressBase() {
		return voidBatteryStressBase;
	}

	public static int getVoidBatteryStressAtFullChannel() {
		return voidBatteryStressAtFullChannel;
	}

	public static int getVoidBatteryTransferFluidDrainMbPerTick() {
		return voidBatteryTransferFluidDrainMbPerTick;
	}

	public static int getVoidTankStressBase() {
		return voidTankStressBase;
	}

	public static int getVoidTankStressAtFullChannel() {
		return voidTankStressAtFullChannel;
	}

	public static int getVoidTankTransferFluidDrainMbPerTick() {
		return voidTankTransferFluidDrainMbPerTick;
	}

	private static void load() {
		voidMotorInputLocalStressPercent = VOID_MOTOR_INPUT_LOCAL_STRESS_PERCENT.get();
		voidMotorInputTransferFluidDrainPercent = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_PERCENT.get();
		voidMotorInputTransferFluidDrainMinMbPerTick = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_MIN_MB.get();
		voidChestStressBase = VOID_CHEST_STRESS_BASE.get();
		voidChestStressAtFullChannel = VOID_CHEST_STRESS_AT_FULL_CHANNEL.get();
		voidChestTransferFluidDrainMbPerTick = VOID_CHEST_TRANSFER_FLUID_DRAIN_MB.get();
		voidBatteryStressBase = VOID_BATTERY_STRESS_BASE.get();
		voidBatteryStressAtFullChannel = VOID_BATTERY_STRESS_AT_FULL_CHANNEL.get();
		voidBatteryTransferFluidDrainMbPerTick = VOID_BATTERY_TRANSFER_FLUID_DRAIN_MB.get();
		voidTankStressBase = VOID_TANK_STRESS_BASE.get();
		voidTankStressAtFullChannel = VOID_TANK_STRESS_AT_FULL_CHANNEL.get();
		voidTankTransferFluidDrainMbPerTick = VOID_TANK_TRANSFER_FLUID_DRAIN_MB.get();
	}

	@SubscribeEvent
	static void onLoad(ModConfigEvent.Loading event) {
		if (event.getConfig().getSpec() == SPEC)
			load();
	}

	@SubscribeEvent
	static void onReload(ModConfigEvent.Reloading event) {
		if (event.getConfig().getSpec() == SPEC)
			load();
	}

}

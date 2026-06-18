package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.VoidwayClient;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBattery;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VoidBatteryUpdatePacket implements CustomPacketPayload {

	public static final Type<VoidBatteryUpdatePacket> TYPE = new Type<>(VoidwayMod.asResource("void_battery_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidBatteryUpdatePacket> STREAM_CODEC =
			StreamCodec.ofMember(VoidBatteryUpdatePacket::write, VoidBatteryUpdatePacket::new);

	private final NetworkKey key;
	private final CompoundTag batteryTag;

	public VoidBatteryUpdatePacket(NetworkKey key, VoidBattery battery) {
		this(key, battery.serializeNBT());
	}

	private VoidBatteryUpdatePacket(NetworkKey key, CompoundTag batteryTag) {
		this.key = key;
		this.batteryTag = batteryTag;
	}

	private VoidBatteryUpdatePacket(RegistryFriendlyByteBuf buffer) {
		this(NetworkKey.fromBuffer(buffer), buffer.readNbt());
	}

	private void write(RegistryFriendlyByteBuf buffer) {
		key.writeToBuffer(buffer);
		buffer.writeNbt(batteryTag);
	}

	public static void handle(VoidBatteryUpdatePacket packet, IPayloadContext context) {
		VoidBattery battery = new VoidBattery(packet.key);
		battery.deserializeNBT(packet.batteryTag);
		VoidwayClient.VOID_BATTERIES.storages.put(packet.key, battery);
	}

	@Override
	public Type<VoidBatteryUpdatePacket> type() {
		return TYPE;
	}
}

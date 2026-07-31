package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayClient;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VoidTankUpdatePacket implements CustomPacketPayload {

	public static final Type<VoidTankUpdatePacket> TYPE = new Type<>(VoidwayMod.asResource("void_tank_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidTankUpdatePacket> STREAM_CODEC =
			StreamCodec.ofMember(VoidTankUpdatePacket::write, VoidTankUpdatePacket::new);

	private final NetworkKey key;
	private final CompoundTag tankTag;

	public VoidTankUpdatePacket(NetworkKey key, VoidTank tank, HolderLookup.Provider registries) {
		this(key, tank.writeToNBT(registries, new CompoundTag()));
	}

	private VoidTankUpdatePacket(NetworkKey key, CompoundTag tankTag) {
		this.key = key;
		this.tankTag = tankTag != null ? tankTag : new CompoundTag();
	}

	private VoidTankUpdatePacket(RegistryFriendlyByteBuf buffer) {
		NetworkKey networkKey = NetworkKey.fromBuffer(buffer);
		CompoundTag tag = buffer.readNbt();
		this.key = networkKey;
		this.tankTag = tag != null ? tag : new CompoundTag();
	}

	private void write(RegistryFriendlyByteBuf buffer) {
		key.writeToBuffer(buffer);
		buffer.writeNbt(tankTag);
	}

	public static void handle(VoidTankUpdatePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!FMLEnvironment.dist.isClient())
				return;
			var level = Minecraft.getInstance().level;
			if (level == null)
				return;
			VoidTank tank = new VoidTank(packet.key);
			tank.readFromNBT(level.registryAccess(), packet.tankTag);
			VoidwayClient.VOID_TANKS.storages.put(packet.key, tank);
		});
	}

	@Override
	public Type<VoidTankUpdatePacket> type() {
		return TYPE;
	}
}

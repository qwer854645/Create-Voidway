package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayClient;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestContainer;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInventory;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestStorage;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VoidChestUpdatePacket implements CustomPacketPayload {

	public static final Type<VoidChestUpdatePacket> TYPE = new Type<>(VoidwayMod.asResource("void_chest_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidChestUpdatePacket> STREAM_CODEC =
			StreamCodec.ofMember(VoidChestUpdatePacket::write, VoidChestUpdatePacket::new);

	private final NetworkKey key;
	private final CompoundTag inventoryTag;

	public VoidChestUpdatePacket(NetworkKey key, VoidChestInventory inventory, HolderLookup.Provider registries) {
		this(key, inventory.serializeNBT(registries));
	}

	private VoidChestUpdatePacket(NetworkKey key, CompoundTag inventoryTag) {
		this.key = key;
		this.inventoryTag = inventoryTag;
	}

	private VoidChestUpdatePacket(RegistryFriendlyByteBuf buffer) {
		this(NetworkKey.fromBuffer(buffer), buffer.readNbt());
	}

	private void write(RegistryFriendlyByteBuf buffer) {
		key.writeToBuffer(buffer);
		buffer.writeNbt(inventoryTag);
	}

	public static void handle(VoidChestUpdatePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			var level = Minecraft.getInstance().level;
			if (level == null)
				return;

			VoidChestInventory inventory = VoidChestStorage.of(packet.key, true);
			inventory.deserializeNBT(level.registryAccess(), packet.inventoryTag);

			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && mc.player.containerMenu instanceof VoidChestContainer menu
					&& menu.isDisplayingNetwork(packet.key))
				menu.onStorageUpdated();
		});
	}

	@Override
	public Type<VoidChestUpdatePacket> type() {
		return TYPE;
	}

}

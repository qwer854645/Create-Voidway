package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;

public class VoidMotorNetworkHandler {

	static final Map<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> connections = new HashMap<>();

	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = actor.getNetworkKey();
		return networksInWorld.computeIfAbsent(key, $ -> new LinkedHashSet<>());
	}

	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		return connections.computeIfAbsent(WorldHelper.getDimensionID(world), $ -> new HashMap<>());
	}

	public void onLoadWorld(LevelAccessor world) {
		networksIn(world);
		Create.LOGGER.debug("Prepared Void Motor Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		connections.remove(WorldHelper.getDimensionID(world));
		Create.LOGGER.debug("Removed Void Motor Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void addToNetwork(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		getNetworkOf(world, actor).add(actor.getPos());
		updateNetworkOf(world, actor);
	}

	public void removeFromNetwork(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		if (actor.blockEntity instanceof IVoidMotorRelay relay)
			relay.clearChannelStress();

		Set<BlockPos> network = getNetworkOf(world, actor);
		network.remove(actor.getPos());
		if (network.isEmpty())
			networksIn(world).remove(actor.getNetworkKey());
		else
			updateNetworkOf(world, actor);

		if (actor.blockEntity instanceof IVoidMotorRelay relay)
			relay.setLinkedPartners(0);
	}

	/**
	 * Sum stress from inputs, then grant each output its requested RPM if the channel can afford it.
	 * When total demand exceeds supply, all outputs are scaled down proportionally.
	 */
	public void updateNetworkOf(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		Set<BlockPos> network = getNetworkOf(world, actor);

		for (Iterator<BlockPos> iterator = network.iterator(); iterator.hasNext(); ) {
			BlockPos pos = iterator.next();
			if (!isAlive(world, pos))
				iterator.remove();
		}

		float totalStressIn = 0;
		List<VoidMotorOutputTileEntity> outputs = new ArrayList<>();

		for (BlockPos pos : network) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof VoidMotorInputTileEntity input && input.isRelayAlive())
				totalStressIn += Math.abs(input.getChannelStressContribution());
			else if (blockEntity instanceof VoidMotorOutputTileEntity output && output.isRelayAlive())
				outputs.add(output);
		}

		float totalDemand = 0;
		for (VoidMotorOutputTileEntity output : outputs)
			totalDemand += output.getRequestedChannelStress();

		float scale = totalDemand <= 1e-4f || totalStressIn >= totalDemand
				? 1f
				: totalStressIn / totalDemand;

		for (VoidMotorOutputTileEntity output : outputs) {
			int requested = output.getRequestedRpm();
			float granted = requested * scale;
			if (granted != 0)
				granted = Math.copySign((float) Math.floor(Math.abs(granted)), granted);
			output.applyGrantedSpeed(granted);
			float used = (float) Math.floor(VoidMotorOutputTileEntity.OUTPUT_STRESS_CAPACITY * Math.abs(granted));
			output.setChannelStressStats(totalStressIn, used);
		}

		for (BlockPos pos : network) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof VoidMotorInputTileEntity input) {
				input.setChannelStressStats(totalStressIn, 0);
				input.updateLinkedPartnerCount(world);
			}
		}

		for (VoidMotorOutputTileEntity output : outputs)
			output.updateLinkedPartnerCount(world);
	}

	public int countLinkedPartners(LevelAccessor world, VoidMotorLinkBehaviour actor, boolean wantOutputs) {
		int count = 0;
		BlockPos self = actor.getPos();
		for (BlockPos pos : getNetworkOf(world, actor)) {
			if (pos.equals(self) || !isAlive(world, pos))
				continue;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof IVoidMotorRelay relay))
				continue;
			if (relay.isVoidMotorOutput() == wantOutputs)
				count++;
		}
		return count;
	}

	private static boolean isAlive(LevelAccessor world, BlockPos pos) {
		if (!world.hasChunkAt(pos))
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity != null && !blockEntity.isRemoved();
	}

	public static class NetworkKey {

		@Nullable
		public final GameProfile owner;
		public final Couple<Frequency> frequencies;

		public NetworkKey(@Nullable GameProfile owner, Frequency frequencyFirst, Frequency frequencySecond) {
			this.owner = owner;
			this.frequencies = Couple.create(frequencyFirst, frequencySecond);
		}

		public void writeToBuffer(RegistryFriendlyByteBuf buffer) {
			buffer.writeNbt(serialize(buffer.registryAccess()));
		}

		public static NetworkKey fromBuffer(RegistryFriendlyByteBuf buffer) {
			return deserialize(buffer.registryAccess(), buffer.readNbt());
		}

		@Override
		public int hashCode() {
			return Objects.hash(owner, frequencies);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			NetworkKey other = (NetworkKey) obj;
			return Objects.equals(owner, other.owner) && frequencies.equals(other.frequencies);
		}

		public CompoundTag serialize(HolderLookup.Provider registries) {
			CompoundTag tag = new CompoundTag();
			if (owner != null) {
				if (owner.getId() != null)
					tag.putUUID("OwnerId", owner.getId());
				if (owner.getName() != null)
					tag.putString("OwnerName", owner.getName());
			}
			putFrequency(tag, registries, "FrequencyFirst", frequencies.get(true));
			putFrequency(tag, registries, "FrequencyLast", frequencies.get(false));
			return tag;
		}

		public static NetworkKey deserialize(HolderLookup.Provider registries, CompoundTag tag) {
			Frequency frequencyFirst = readFrequency(tag, registries, "FrequencyFirst");
			Frequency frequencyLast = readFrequency(tag, registries, "FrequencyLast");
			GameProfile owner = null;
			if (tag.contains("OwnerId") || tag.contains("OwnerName"))
				owner = new GameProfile(tag.contains("OwnerId") ? tag.getUUID("OwnerId") : null,
						tag.contains("OwnerName") ? tag.getString("OwnerName") : null);
			return new NetworkKey(owner, frequencyFirst, frequencyLast);
		}

		private static void putFrequency(CompoundTag tag, HolderLookup.Provider registries, String key, Frequency frequency) {
			ItemStack stack = frequency.getStack();
			if (stack.isEmpty()) {
				tag.remove(key);
				return;
			}
			tag.put(key, stack.save(registries));
		}

		private static Frequency readFrequency(CompoundTag tag, HolderLookup.Provider registries, String key) {
			if (!tag.contains(key, Tag.TAG_COMPOUND)) {
				return Frequency.EMPTY;
			}
			return Frequency.of(ItemStack.parseOptional(registries, tag.getCompound(key)));
		}

	}

}

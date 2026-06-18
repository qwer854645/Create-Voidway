package com.xeli.createvoidway.blocks.teleport;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidTeleportLinkTileEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	protected VoidTeleportLinkBehaviour link;
	@Nullable
	private BlockPos boundPadPos;

	public VoidTeleportLinkTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						blockState -> blockState.getValue(VoidTeleportLinkBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));
		link = new VoidTeleportLinkBehaviour(this, slots);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	public VoidTeleportLinkBehaviour getTeleportLink() {
		return link;
	}

	@Nullable
	public BlockPos getBoundPadPos() {
		return boundPadPos;
	}

	public void setBoundPadPos(@Nullable BlockPos padPos) {
		this.boundPadPos = padPos;
		setChanged();
		sendData();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide)
			VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.validateLinkBinding(level, worldPosition);
	}

	public boolean isBoundTo(BlockPos padPos) {
		return boundPadPos != null && boundPadPos.equals(padPos);
	}

	public boolean isMutuallyBound() {
		if (boundPadPos == null || level == null)
			return false;
		if (!(level.getBlockEntity(boundPadPos) instanceof VoidTeleportPadTileEntity pad))
			return false;
		return pad.isBoundTo(worldPosition) && VoidTeleportNetworkHandler.areAdjacent(worldPosition, boundPadPos);
	}

	public boolean hasFrequencyConfigured() {
		return !link.getFrequencyStack(true).isEmpty() && !link.getFrequencyStack(false).isEmpty();
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (tag.contains("BoundPadPos"))
			boundPadPos = BlockPos.of(tag.getLong("BoundPadPos"));
		else
			boundPadPos = null;
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (boundPadPos != null)
			tag.putLong("BoundPadPos", boundPadPos.asLong());
		super.write(tag, registries, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidTeleportLinkGoggleTooltip.add(tooltip, this);
		return true;
	}

}

package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public abstract class AbstractVoidMotorTileEntity extends KineticBlockEntity implements IVoidMotorRelay {

	protected VoidMotorLinkBehaviour link;
	protected int linkedPartners;
	protected float channelStressTotal;
	protected float channelStressUsed;

	protected AbstractVoidMotorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						s -> s.getValue(DirectionalKineticBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));
		link = new VoidMotorLinkBehaviour(this, slots);
	}

	@Override
	public VoidMotorLinkBehaviour getVoidMotorLink() {
		return link;
	}

	@Override
	public int getLinkedPartners() {
		return linkedPartners;
	}

	@Override
	public void setLinkedPartners(int partners) {
		if (linkedPartners == partners)
			return;
		linkedPartners = partners;
		sendData();
	}

	@Override
	public void setChannelStressStats(float total, float usedStress) {
		if (channelStressTotal == total && channelStressUsed == usedStress)
			return;
		channelStressTotal = total;
		channelStressUsed = usedStress;
		sendData();
	}

	@Override
	public void updateLinkedPartnerCount(LevelAccessor world) {
		if (level == null || level.isClientSide)
			return;
		int partners = getHandler().countLinkedPartners(world, link, !isVoidMotorOutput());
		if (partners == linkedPartners)
			return;
		linkedPartners = partners;
		sendData();
	}

	protected VoidMotorNetworkHandler getHandler() {
		return com.xeli.createvoidway.VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER;
	}

	protected void notifyRelayNetwork() {
		if (level != null && !level.isClientSide && link != null)
			getHandler().updateNetworkOf(level, link);
	}

	@Override
	public boolean isRelayAlive() {
		return level != null && !isRemoved() && level.isLoaded(worldPosition) && level.getBlockEntity(worldPosition) == this;
	}

	@Override
	public float getChannelStressContribution() {
		return 0;
	}

	@Override
	public void clearChannelStress() {
		setChannelStressStats(0, 0);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		linkedPartners = tag.getInt("LinkedPartners");
		channelStressTotal = tag.getFloat("ChannelStressTotal");
		channelStressUsed = tag.getFloat("ChannelStressUsed");
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.putInt("LinkedPartners", linkedPartners);
		tag.putFloat("ChannelStressTotal", channelStressTotal);
		tag.putFloat("ChannelStressUsed", channelStressUsed);
		super.write(tag, registries, clientPacket);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

}

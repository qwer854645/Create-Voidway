package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayClient;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.IVoidStorageRelay;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageGoggleTooltip;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageKind;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageLinkBehaviour;
import com.xeli.createvoidway.config.VoidChannelStress;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractVoidTankTileEntity extends KineticBlockEntity implements IHaveGoggleInformation, IVoidStorageRelay {

	protected VoidStorageLinkBehaviour link;
	protected int linkedPartners;

	protected AbstractVoidTankTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						blockState -> Direction.DOWN,
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));
		link = new VoidStorageLinkBehaviour(this, slots);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	@Override
	public VoidStorageKind getStorageKind() {
		return VoidStorageKind.TANK;
	}

	@Override
	public boolean isStorageOutput() {
		return !isVoidTankInput();
	}

	@Override
	public VoidStorageLinkBehaviour getStorageLink() {
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
	public void updateLinkedPartnerCount(LevelAccessor world) {
		if (level == null || level.isClientSide)
			return;
		int partners = VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.countLinkedPartners(world, link);
		if (partners == linkedPartners)
			return;
		linkedPartners = partners;
		sendData();
	}

	@Override
	public boolean isRelayAlive() {
		return level != null && !isRemoved() && level.isLoaded(worldPosition) && level.getBlockEntity(worldPosition) == this;
	}

	public float getChannelFillRatio() {
		VoidTank tank = getFluidStorage();
		int capacity = tank.getCapacity();
		if (capacity <= 0)
			return 0;
		return tank.getFluidAmount() / (float) capacity;
	}

	public int getChannelStressDemand() {
		return VoidChannelStress.computeDemand(
				VoidwayConfig.getVoidTankStressBase(),
				VoidwayConfig.getVoidTankStressAtFullChannel(),
				getChannelFillRatio());
	}

	public abstract boolean isVoidTankInput();

	public boolean hasRequiredStress() {
		return hasShaftConnection() && hasSource() && !isOverStressed()
				&& Math.abs(getTheoreticalSpeed()) > 0;
	}

	public boolean canOperate() {
		return hasRequiredStress();
	}

	public VoidTank getFluidStorage() {
		return hasPersistentData() ?
				VoidwayMod.VOID_TANKS_DATA.computeStorageIfAbsent(link.getNetworkKey()) :
				VoidwayClient.VOID_TANKS.computeStorageIfAbsent(link.getNetworkKey());
	}

	public IFluidHandler getStorageFluidHandler() {
		VoidTankFilteredFluidHandler.Mode mode = VoidTankFilteredFluidHandler.Mode.BLOCKED;
		if (canOperate()) {
			mode = isVoidTankInput()
					? VoidTankFilteredFluidHandler.Mode.INSERT_ONLY
					: VoidTankFilteredFluidHandler.Mode.EXTRACT_ONLY;
		}
		return new VoidTankFilteredFluidHandler(getFluidStorage(), mode);
	}

	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction side) {
		return getStorageFluidHandler();
	}

	public boolean isClosed() {
		return getBlockState().getValue(AbstractVoidTankBlock.CLOSED);
	}

	private boolean hasPersistentData() {
		return level != null && !level.isClientSide;
	}

	@Override
	public float calculateStressApplied() {
		if (!hasShaftConnection() || Math.abs(getTheoreticalSpeed()) == 0)
			return 0;
		return VoidChannelStress.toImpactPerRpm(getChannelStressDemand(), getTheoreticalSpeed());
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket)
			getFluidStorage().readFromNBT(registries, tag.getCompound("Tank"));
		linkedPartners = tag.getInt("LinkedPartners");
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket)
			tag.put("Tank", getFluidStorage().writeToNBT(registries, new CompoundTag()));
		tag.putInt("LinkedPartners", linkedPartners);
		super.write(tag, registries, clientPacket);
	}

	public boolean hasShaftConnection() {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof AbstractVoidTankBlock<?> block))
			return false;
		return block.hasShaftTowards(level, worldPosition, state, Direction.DOWN);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidStorageGoggleTooltip.addRoleAndLink(tooltip, "void_tank", isVoidTankInput(), linkedPartners);

		boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, getFluidStorage());

		int speed = (int) Math.abs(getTheoreticalSpeed());
		VoidStorageGoggleTooltip.addKineticStatus(tooltip, "void_tank", speed,
				getChannelStressDemand(), 0,
				hasShaftConnection(), hasSource(), isOverStressed(), hasRequiredStress(),
				true, canOperate());

		return added;
	}

}

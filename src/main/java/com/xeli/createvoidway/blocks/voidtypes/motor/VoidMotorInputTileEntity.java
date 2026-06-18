package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * Injects local rotational stress into a void channel while rotating.
 */
public class VoidMotorInputTileEntity extends AbstractVoidMotorTileEntity implements IHaveGoggleInformation {

	public static final int FLUID_CAPACITY = 4000;
	public static final float RELAY_STRESS_IMPACT = 8.0f;

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(FLUID_CAPACITY, () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	private boolean transferFluidWasEmpty = true;

	public VoidMotorInputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						VoidMotorInputBlock::getLinkSlotFace,
						VecHelper.voxelSpace(5.5F, 10.5F, 16.001F)));
		link = new VoidMotorLinkBehaviour(this, slots);
	}

	@Override
	public boolean isVoidMotorOutput() {
		return false;
	}

	public FluidTank getFluidTank() {
		return fluidTank;
	}

	public boolean canRelay() {
		return hasRotation() && hasSufficientTransferFluid();
	}

	private boolean hasRotation() {
		return getTheoreticalSpeed() != 0;
	}

	private boolean hasSufficientTransferFluid() {
		if (getPotentialChannelStressContribution() <= 0)
			return false;
		FluidStack stored = fluidTank.getFluid();
		if (stored.isEmpty() || !RWFluids.isAllowedInVoidMotorInput(stored))
			return false;
		return fluidTank.getFluidAmount() >= getTransferFluidDrainThisTick();
	}

	public int getTransferFluidDrainThisTick() {
		if (!hasRotation())
			return 0;
		float contribution = getPotentialChannelStressContribution();
		if (contribution <= 0)
			return 0;
		int percentDrain = (int) Math.floor(contribution * VoidwayConfig.getInputTransferFluidDrainPercent() / 100f);
		return Math.max(percentDrain, VoidwayConfig.getInputTransferFluidDrainMinMbPerTick());
	}

	public float getPotentialChannelStressContribution() {
		if (getTheoreticalSpeed() == 0)
			return 0;
		return (float) Math.floor(getReceivedStress() * VoidwayConfig.getInputNetworkContributionFraction());
	}

	public float getReceivedStress() {
		float speed = getTheoreticalSpeed();
		if (speed == 0)
			return 0;
		return RELAY_STRESS_IMPACT * Math.abs(speed);
	}

	public float getLocalStressConsumption() {
		if (!canRelay())
			return 0;
		return (float) Math.floor(getReceivedStress() * VoidwayConfig.getInputLocalStressFraction());
	}

	@Override
	public float getChannelStressContribution() {
		if (!canRelay())
			return 0;
		return getPotentialChannelStressContribution();
	}

	@Override
	public float calculateStressApplied() {
		if (!canRelay())
			return 0;
		float speed = Math.abs(getTheoreticalSpeed());
		if (speed == 0)
			return 0;
		return getLocalStressConsumption() / speed;
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		notifyRelayNetwork();
	}

	private boolean isRelayingRotation() {
		return canRelay() && getTheoreticalSpeed() != 0;
	}

	private void consumeTransferFluid() {
		fluidTank.drain(getTransferFluidDrainThisTick(), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		boolean canRelay = canRelay();
		if (canRelay && transferFluidWasEmpty) {
			transferFluidWasEmpty = false;
			notifyRelayNetwork();
		}
		if (!canRelay && !transferFluidWasEmpty) {
			transferFluidWasEmpty = true;
			notifyRelayNetwork();
		}

		if (isRelayingRotation())
			consumeTransferFluid();

		if (level.getGameTime() % 20 == 0)
			notifyRelayNetwork();
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (tag.contains("FluidTank"))
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
		else if (tag.contains("CatalystTank"))
			fluidTank.readFromNBT(registries, tag.getCompound("CatalystTank"));
		fluidTank.purgeInvalidContents();
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		super.write(tag, registries, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidMotorGoggleTooltip.addInputRoleAndLink(tooltip, linkedPartners);

		if (linkedPartners > 0)
			VoidMotorGoggleTooltip.addChannelStressTotal(tooltip, (int) channelStressTotal);

		boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, fluidTank);

		int speed = (int) Math.abs(getTheoreticalSpeed());
		VoidMotorGoggleTooltip.addInputInjectionStatus(tooltip, speed,
				(int) getReceivedStress(),
				(int) getLocalStressConsumption(),
				(int) (canRelay() ? getChannelStressContribution() : getPotentialChannelStressContribution()),
				getTransferFluidDrainThisTick(),
				hasSource(), isOverStressed(), hasSufficientTransferFluid(), canRelay());

		return added;
	}

	public IFluidHandler getFluidHandler(Direction side) {
		return fluidTank;
	}

}

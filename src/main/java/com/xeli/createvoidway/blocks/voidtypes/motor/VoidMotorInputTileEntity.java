package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
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

import java.util.List;

/**
 * Injects local rotational stress into a void channel while rotating.
 */
public class VoidMotorInputTileEntity extends AbstractVoidMotorTileEntity implements IHaveGoggleInformation {

	public static final int FLUID_CAPACITY = 4000;

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(FLUID_CAPACITY, () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	private boolean transferFluidWasEmpty = true;
	private float appliedDemand = -1;
	private boolean previouslyOverstressed;
	private float receivedStress;

	public VoidMotorInputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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

	@Override
	public boolean isLocallyReady() {
		return canRelay();
	}

	/** Locally able to inject and at least one ready output is linked. */
	public boolean isInjecting() {
		return canRelay() && readyPartners > 0;
	}

	private boolean hasRotation() {
		return hasSource() && getTheoreticalSpeed() != 0;
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
		float contribution = (float) Math.floor(getReceivedStress() * VoidwayConfig.getInputNetworkContributionFraction());
		return Math.min(contribution, VoidwayConfig.getVoidMotorInputMaxStressCapacity());
	}

	public float getReceivedStress() {
		if (level == null || level.isClientSide)
			return receivedStress;
		if (!hasRotation() || !hasNetwork())
			return 0;
		var kineticNetwork = getOrCreateNetwork();
		float totalStress = kineticNetwork.calculateStress();
		float relayStress = 0;
		float totalLimit = 0;
		boolean countedSelf = false;
		for (var member : kineticNetwork.members.keySet()) {
			if (!(member instanceof VoidMotorInputTileEntity input))
				continue;
			relayStress += kineticNetwork.getActualStressOf(input);
			// Inspect raw conditions only: calling canRelay here would recurse into this allocation.
			if (input == this || (input.hasRotation() && input.readyPartners > 0
					&& input.fluidTank.getFluidAmount() > 0
					&& RWFluids.isAllowedInVoidMotorInput(input.fluidTank.getFluid()))) {
				totalLimit += input.getInputStressLimit();
				countedSelf |= input == this;
			}
		}
		if (!countedSelf)
			totalLimit += getInputStressLimit();
		return VoidMotorStressBudget.allocate(kineticNetwork.calculateCapacity(), totalStress,
				relayStress, getInputStressLimit(), totalLimit);
	}

	private float getInputStressLimit() {
		float speed = getTheoreticalSpeed();
		if (speed == 0)
			return 0;
		float raw = VoidwayConfig.getVoidMotorInputStressPerRpm() * Math.abs(speed);
		return Math.min(raw, VoidwayConfig.getVoidMotorInputMaxStressCapacity());
	}

	public float getLocalStressConsumption() {
		if (!isInjecting())
			return 0;
		return (float) Math.floor(getReceivedStress() * VoidwayConfig.getInputLocalStressFraction());
	}

	@Override
	public float getChannelStressContribution() {
		if (!isInjecting() || !hasSource() || isOverStressed())
			return 0;
		return getPotentialChannelStressContribution();
	}

	@Override
	public float calculateStressApplied() {
		if (!isInjecting())
			return 0;
		float speed = Math.abs(getTheoreticalSpeed());
		if (speed == 0)
			return 0;
		// Charge the source for both transmitted stress and the local conversion loss.
		// Keep this demand while overloaded so the channel cannot repeatedly restart itself.
		return getReceivedStress() / speed;
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		notifyRelayNetwork();
	}

	private boolean isRelayingRotation() {
		return isInjecting() && !isOverStressed() && getTheoreticalSpeed() != 0;
	}

	private void consumeTransferFluid() {
		fluidTank.drain(getTransferFluidDrainThisTick(), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		float available = getReceivedStress();
		if (Float.compare(available, receivedStress) != 0) {
			receivedStress = available;
			sendData();
			notifyRelayNetwork();
		}

		if (hasNetwork()) {
			float demand = calculateStressApplied();
			if (Float.compare(demand, appliedDemand) != 0) {
				appliedDemand = demand;
				lastStressApplied = demand;
				getOrCreateNetwork().updateStressFor(this, demand);
			}
		}
		if (previouslyOverstressed != isOverStressed()) {
			previouslyOverstressed = isOverStressed();
			notifyRelayNetwork();
		}

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
		receivedStress = Math.max(0, tag.getFloat("ReceivedStress"));
		if (tag.contains("FluidTank"))
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
		else if (tag.contains("CatalystTank"))
			fluidTank.readFromNBT(registries, tag.getCompound("CatalystTank"));
		fluidTank.purgeInvalidContents();
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		tag.putFloat("ReceivedStress", receivedStress);
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
				(int) (isInjecting() ? getChannelStressContribution() : getPotentialChannelStressContribution()),
				getTransferFluidDrainThisTick(),
				hasSource(), isOverStressed(), hasSufficientTransferFluid(),
				canRelay(), isInjecting(), readyPartners > 0);

		return added;
	}

	public IFluidHandler getFluidHandler(Direction side) {
		return fluidTank;
	}

}

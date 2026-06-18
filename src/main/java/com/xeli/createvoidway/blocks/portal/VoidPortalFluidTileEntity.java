package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidPortalFluidTileEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(
			VoidwayConfig.getVoidTeleportFluidCapacity(), () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	public VoidPortalFluidTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	public VoidTransferFluidTank getFluidTank() {
		return fluidTank;
	}

	public boolean acceptsFluidFrom(@Nullable Direction side) {
		return side != null && side.getAxis().isHorizontal();
	}

	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction side) {
		return acceptsFluidFrom(side) ? fluidTank : null;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (tag.contains("FluidTank")) {
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
			fluidTank.purgeInvalidContents();
		}
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		super.write(tag, registries, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidPortalGoggleTooltip.addFluid(tooltip, this, isPlayerSneaking);
		return true;
	}

}

package com.xeli.createvoidway.blocks.voidtypes.chest;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.IVoidStorageRelay;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageGoggleTooltip;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageKind;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageLinkBehaviour;
import com.xeli.createvoidway.config.VoidChannelStress;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractVoidChestTileEntity extends KineticBlockEntity
		implements MenuProvider, IHaveGoggleInformation, IVoidStorageRelay {

	public static final int FLUID_CAPACITY = 4000;

	protected VoidStorageLinkBehaviour link;
	protected int linkedPartners;

	private int openCount;
	public LerpedFloat lid = LerpedFloat.linear().startWithValue(0);

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(FLUID_CAPACITY, () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	protected AbstractVoidChestTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						state -> state.getValue(AbstractVoidChestBlock.FACING),
						VecHelper.voxelSpace(5.5F, 7.5F, .999F)));
		link = new VoidStorageLinkBehaviour(this, slots);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	@Override
	public VoidStorageKind getStorageKind() {
		return VoidStorageKind.CHEST;
	}

	@Override
	public boolean isStorageOutput() {
		return !isVoidChestInput();
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

	public int countOccupiedSlots() {
		VoidChestInventory storage = getItemStorage();
		int count = 0;
		for (int i = 0; i < storage.getSlots(); i++) {
			if (!storage.getStackInSlot(i).isEmpty())
				count++;
		}
		return count;
	}

	public float getChannelFillRatio() {
		VoidChestInventory storage = getItemStorage();
		int slots = storage.getSlots();
		if (slots <= 0)
			return 0;
		return countOccupiedSlots() / (float) slots;
	}

	public int getChannelStressDemand() {
		return VoidChannelStress.computeDemand(
				VoidwayConfig.getVoidChestStressBase(),
				VoidwayConfig.getVoidChestStressAtFullChannel(),
				getChannelFillRatio());
	}

	public abstract boolean isVoidChestInput();

	public abstract Component getMenuTitle();

	public boolean hasRequiredStress() {
		return hasShaftConnection() && hasSource() && !isOverStressed()
				&& Math.abs(getTheoreticalSpeed()) > 0;
	}

	public boolean hasSufficientTransferFluid() {
		if (!hasRequiredStress())
			return false;
		FluidStack stored = fluidTank.getFluid();
		if (stored.isEmpty() || !RWFluids.isAllowedInVoidMotorInput(stored))
			return false;
		return fluidTank.getFluidAmount() >= getTransferFluidDrainThisTick();
	}

	public int getTransferFluidDrainThisTick() {
		if (!hasRequiredStress())
			return 0;
		return VoidwayConfig.getVoidChestTransferFluidDrainMbPerTick();
	}

	public boolean canOperate() {
		return hasRequiredStress() && hasSufficientTransferFluid();
	}

	public FluidTank getFluidTank() {
		return fluidTank;
	}

	public boolean acceptsFluidFrom(Direction side) {
		if (side == null)
			return false;
		Direction facing = getBlockState().getValue(AbstractVoidChestBlock.FACING);
		return side == facing.getClockWise() || side == facing.getCounterClockWise();
	}

	@Nullable
	public IFluidHandler getFluidHandler(Direction side) {
		if (!acceptsFluidFrom(side))
			return null;
		return fluidTank;
	}

	public IItemHandler getAutomationHandler() {
		VoidChestFilteredHandler.Mode mode = VoidChestFilteredHandler.Mode.BLOCKED;
		if (canOperate()) {
			mode = isVoidChestInput() ? VoidChestFilteredHandler.Mode.INSERT_ONLY
					: VoidChestFilteredHandler.Mode.EXTRACT_ONLY;
		}
		return new VoidChestFilteredHandler(getItemStorage(), mode);
	}

	@Override
	public float calculateStressApplied() {
		if (!hasShaftConnection() || Math.abs(getTheoreticalSpeed()) == 0)
			return 0;
		return VoidChannelStress.toImpactPerRpm(getChannelStressDemand(), getTheoreticalSpeed());
	}

	public VoidChestInventory getItemStorage() {
		return VoidChestStorage.of(link, level != null && level.isClientSide);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket) {
			getItemStorage().deserializeNBT(registries, tag.getCompound("Inventory"));
			openCount = tag.getInt("OpenCount");
		}
		linkedPartners = tag.getInt("LinkedPartners");
		if (tag.contains("FluidTank")) {
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
			fluidTank.purgeInvalidContents();
		}
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket) {
			tag.put("Inventory", getItemStorage().serializeNBT(registries));
			tag.putInt("OpenCount", openCount);
		}
		tag.putInt("LinkedPartners", linkedPartners);
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));

		super.write(tag, registries, clientPacket);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return getMenuTitle();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
		if (!canOperate())
			return null;
		return VoidChestContainer.create(id, playerInventory, this);
	}

	@Override
	public void tick() {
		super.tick();
		if (level != null && !level.isClientSide && hasRequiredStress())
			fluidTank.drain(getTransferFluidDrainThisTick(), IFluidHandler.FluidAction.EXECUTE);
		lid.chase(openCount > 0 ? 1 : 0, 0.1f, LerpedFloat.Chaser.LINEAR);
		lid.tickChaser();
	}

	public boolean isClosed() {
		return lid.settled() && lid.getChaseTarget() == 0;
	}

	public void startOpen(Player player) {
		if (openCount < 0)
			openCount = 0;
		openCount++;
		sendData();
		if (openCount == 1) {
			level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition);
			level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F,
					level.random.nextFloat() * 0.1F + 0.9F);
		}
	}

	public void stopOpen(Player player) {
		openCount--;
		sendData();
		if (openCount <= 0) {
			level.gameEvent(player, GameEvent.CONTAINER_CLOSE, worldPosition);
			level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
					level.random.nextFloat() * 0.1F + 0.9F);
		}
	}

	public boolean hasShaftConnection() {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof AbstractVoidChestBlock<?> block))
			return false;
		return block.hasShaftTowards(level, worldPosition, state, Direction.DOWN);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidStorageGoggleTooltip.addRoleAndLink(tooltip, "void_chest", isVoidChestInput(), linkedPartners);

		new LangBuilder(VoidwayMod.ID)
				.translate("void_chest.channel_items", countOccupiedSlots(), getItemStorage().getSlots())
				.forGoggles(tooltip);

		boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, fluidTank);

		int speed = (int) Math.abs(getTheoreticalSpeed());
		VoidStorageGoggleTooltip.addKineticStatus(tooltip, "void_chest", speed,
				getChannelStressDemand(), getTransferFluidDrainThisTick(),
				hasShaftConnection(), hasSource(), isOverStressed(), hasRequiredStress(),
				hasSufficientTransferFluid(), canOperate());

		return added;
	}

}

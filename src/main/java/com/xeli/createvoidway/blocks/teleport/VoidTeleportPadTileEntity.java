package com.xeli.createvoidway.blocks.teleport;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportNetworkHandler.PairStatus;
import com.xeli.createvoidway.config.VoidChannelStress;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VoidTeleportPadTileEntity extends KineticBlockEntity
		implements IHaveGoggleInformation, IVoidTeleportPad {

	public static final int FLUID_CAPACITY = 1000;

	private final VoidTeleportPadItemHandler itemHandler = new VoidTeleportPadItemHandler(this);
	private PairStatus pairStatus = PairStatus.UNPAIRED;
	private int padCount;
	@Nullable
	private BlockPos partnerPos;
	@Nullable
	private BlockPos boundLinkPos;
	private int linkDistance;

	private int syncedChargeTicks;

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(
			VoidwayConfig.getVoidTeleportFluidCapacity(), () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	public VoidTeleportPadTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void setNetworkState(PairStatus status, int padCount, @Nullable BlockPos partner, int linkDistance) {
		this.pairStatus = status;
		this.padCount = padCount;
		this.partnerPos = partner;
		this.linkDistance = linkDistance;
		sendData();
	}

	public int getLinkDistance() {
		return linkDistance;
	}

	public int getLinkStressDemand() {
		if (pairStatus != PairStatus.VALID)
			return 0;
		return VoidTeleportLinkMetrics.computeStressDemand(linkDistance);
	}

	@Override
	public PairStatus getPairStatus() {
		return pairStatus;
	}

	@Override
	public int getPadCount() {
		return padCount;
	}

	@Nullable
	@Override
	public BlockPos getPartnerPos() {
		return partnerPos;
	}

	public boolean hasBindingLink() {
		return isMutuallyBound();
	}

	public boolean hasConfiguredBindingLink() {
		if (!isMutuallyBound() || level == null)
			return false;
		VoidTeleportLinkBehaviour link = VoidTeleportNetworkHandler.findBindingLink(level, worldPosition);
		return link != null && VoidTeleportNetworkHandler.hasFrequencyConfigured(link);
	}

	@Nullable
	public BlockPos getBoundLinkPos() {
		return boundLinkPos;
	}

	public void setBoundLinkPos(@Nullable BlockPos linkPos) {
		this.boundLinkPos = linkPos;
		setChanged();
		sendData();
	}

	public boolean isBoundTo(BlockPos linkPos) {
		return boundLinkPos != null && boundLinkPos.equals(linkPos);
	}

	public boolean isMutuallyBound() {
		if (boundLinkPos == null || level == null)
			return false;
		if (!(level.getBlockEntity(boundLinkPos) instanceof VoidTeleportLinkTileEntity link))
			return false;
		return link.isBoundTo(worldPosition) && VoidTeleportNetworkHandler.areAdjacent(worldPosition, boundLinkPos);
	}

	public AABB getContactSearchBounds() {
		return VoidTeleportHelper.getContactSearchBounds(worldPosition);
	}

	public boolean isEntityInContact(Entity entity) {
		return VoidTeleportHelper.isEntityInContactWithPad(worldPosition, entity);
	}

	public int getSyncedChargeTicks() {
		return syncedChargeTicks;
	}

	public boolean hasRequiredStress() {
		return hasShaftConnection() && hasSource() && !isOverStressed()
				&& Math.abs(getTheoreticalSpeed()) > 0;
	}

	public boolean canOperate() {
		return pairStatus == PairStatus.VALID && hasRequiredStress();
	}

	public List<Entity> getPendingBatchEntities() {
		if (level == null)
			return List.of();
		return level.getEntities((Entity) null, getContactSearchBounds(),
				entity -> VoidTeleportHelper.canBeTeleported(entity)
						&& isEntityInContact(entity)
						&& !VoidTeleportHelper.hasContactCooldown(entity));
	}

	public int getPendingBatchFluidCost() {
		return VoidTeleportHelper.computeBatchFluidCost(getPendingBatchEntities());
	}

	public int getPendingBatchEntityCount() {
		return getPendingBatchEntities().size();
	}

	public boolean acceptsFluidFrom(@Nullable Direction side) {
		return side != null && side.getAxis().isHorizontal();
	}

	public VoidTransferFluidTank getFluidTank() {
		return fluidTank;
	}

	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction side) {
		return acceptsFluidFrom(side) ? fluidTank : null;
	}

	@Nullable
	public IItemHandler getItemHandler(@Nullable Direction side) {
		return VoidTeleportPadItemHandler.isInsertSide(side) ? itemHandler : null;
	}

	public ItemStack tryInsertItemForTeleport(ItemStack stack, boolean simulate) {
		if (level == null || level.isClientSide || stack.isEmpty())
			return stack;
		if (!canOperate() || partnerPos == null)
			return stack;

		ServerLevel serverLevel = (ServerLevel) level;
		if (!serverLevel.isLoaded(partnerPos))
			return stack;
		if (!(serverLevel.getBlockEntity(partnerPos) instanceof VoidTeleportPadTileEntity partner))
			return stack;
		if (partner.getPairStatus() != PairStatus.VALID)
			return stack;

		if (!simulate) {
			VoidTeleportHelper.depositItemOnPad(serverLevel, worldPosition, stack);
			setChanged();
			sendData();
		}
		return ItemStack.EMPTY;
	}

	public boolean hasShaftConnection() {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof VoidTeleportPadBlock block))
			return false;
		return block.hasShaftTowards(level, worldPosition, state, Direction.DOWN);
	}

	@Override
	public float calculateStressApplied() {
		if (!hasShaftConnection() || Math.abs(getTheoreticalSpeed()) == 0)
			return 0;
		if (pairStatus != PairStatus.VALID)
			return 0;
		return VoidChannelStress.toImpactPerRpm(getLinkStressDemand(), getTheoreticalSpeed());
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide)
			VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.refreshPadBinding(level, worldPosition);
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.refreshPadBinding(level, worldPosition);
		VoidwayMod.VOID_TELEPORT_NETWORK_HANDLER.tickCooldowns((ServerLevel) level);
		balanceFluidWithPartner((ServerLevel) level);
		processEntities((ServerLevel) level);
	}

	private void balanceFluidWithPartner(ServerLevel serverLevel) {
		int rate = VoidwayConfig.getVoidTeleportFluidShareMbPerTick();
		if (!VoidwayConfig.isVoidTeleportFluidSharingEnabled() || partnerPos == null || pairStatus != PairStatus.VALID || !hasRequiredStress())
			return;
		if (!serverLevel.isLoaded(partnerPos))
			return;
		if (!(serverLevel.getBlockEntity(partnerPos) instanceof VoidTeleportPadTileEntity partner))
			return;

		int mine = fluidTank.getFluidAmount();
		int theirs = partner.fluidTank.getFluidAmount();
		if (mine <= theirs)
			return;

		int toMove = Math.min(rate, mine - theirs);
		if (toMove <= 0)
			return;

		FluidStack drained = fluidTank.drain(toMove, IFluidHandler.FluidAction.EXECUTE);
		if (drained.isEmpty())
			return;
		partner.fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
		partner.setChanged();
		partner.sendData();
		setChanged();
		sendData();
	}

	private void processEntities(ServerLevel serverLevel) {
		List<Entity> batch = getPendingBatchEntities();
		if (batch.isEmpty()) {
			if (syncedChargeTicks != 0) {
				syncedChargeTicks = 0;
				sendData();
			}
			return;
		}

		if (!canOperate()) {
			if (syncedChargeTicks != 0) {
				syncedChargeTicks = 0;
				sendData();
			}
			return;
		}

		int totalCost = VoidTeleportHelper.computeBatchFluidCost(batch);
		if (fluidTank.getFluidAmount() < totalCost) {
			if (syncedChargeTicks != 0) {
				syncedChargeTicks = 0;
				sendData();
			}
			return;
		}

		int maxCharge = VoidwayConfig.getVoidTeleportChargeTicks();
		int charge = syncedChargeTicks + 1;
		if (charge >= maxCharge) {
			if (tryTeleportBatch(serverLevel))
				syncedChargeTicks = 0;
		} else {
			syncedChargeTicks = charge;
		}
		sendData();
	}

	private boolean tryTeleportBatch(ServerLevel level) {
		if (partnerPos == null || !level.isLoaded(partnerPos))
			return false;
		if (!(level.getBlockEntity(partnerPos) instanceof VoidTeleportPadTileEntity partner))
			return false;
		if (partner.getPairStatus() != PairStatus.VALID)
			return false;

		List<Entity> batch = new ArrayList<>();
		for (Entity entity : getPendingBatchEntities()) {
			if (entity.isAlive() && isEntityInContact(entity))
				batch.add(entity);
		}
		if (batch.isEmpty())
			return false;

		int totalCost = VoidTeleportHelper.computeBatchFluidCost(batch);
		if (fluidTank.drain(totalCost, IFluidHandler.FluidAction.SIMULATE).getAmount() < totalCost)
			return false;

		fluidTank.drain(totalCost, IFluidHandler.FluidAction.EXECUTE);
		for (Entity entity : batch)
			VoidTeleportHelper.teleportTo(level, entity, partnerPos, false);

		VoidTeleportHelper.playBatchTeleportEffects(level, worldPosition, partnerPos);
		level.sendParticles(ParticleTypes.PORTAL,
				worldPosition.getX() + 0.5, worldPosition.getY() + VoidTeleportPadBlock.PLATE_HEIGHT + 0.25,
				worldPosition.getZ() + 0.5,
				32, 0.2, 0.5, 0.2, 0.05);
		setChanged();
		sendData();
		return true;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		pairStatus = PairStatus.values()[Math.min(tag.getInt("PairStatus"), PairStatus.values().length - 1)];
		padCount = tag.getInt("PadCount");
		if (tag.contains("PartnerPos"))
			partnerPos = BlockPos.of(tag.getLong("PartnerPos"));
		else
			partnerPos = null;
		if (tag.contains("BoundLinkPos"))
			boundLinkPos = BlockPos.of(tag.getLong("BoundLinkPos"));
		else
			boundLinkPos = null;
		syncedChargeTicks = tag.getInt("ChargeTicks");
		linkDistance = tag.getInt("LinkDistance");
		if (tag.contains("FluidTank")) {
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
			fluidTank.purgeInvalidContents();
		}
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.putInt("PairStatus", pairStatus.ordinal());
		tag.putInt("PadCount", padCount);
		if (partnerPos != null)
			tag.putLong("PartnerPos", partnerPos.asLong());
		if (boundLinkPos != null)
			tag.putLong("BoundLinkPos", boundLinkPos.asLong());
		tag.putInt("ChargeTicks", syncedChargeTicks);
		tag.putInt("LinkDistance", linkDistance);
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		super.write(tag, registries, clientPacket);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidTeleportGoggleTooltip.add(tooltip, this, isPlayerSneaking, fluidTank);
		return true;
	}

}

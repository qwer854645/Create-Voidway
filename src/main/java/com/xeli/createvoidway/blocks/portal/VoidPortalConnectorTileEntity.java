package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidPortalConnectorTileEntity extends SmartBlockEntity
		implements IHaveGoggleInformation, IVoidPortalEndpoint {

	protected VoidPortalLinkBehaviour link;
	@Nullable
	private VoidPortalShape cachedShape;

	private VoidPortalNetworkHandler.PairStatus pairStatus = VoidPortalNetworkHandler.PairStatus.UNPAIRED;
	private int portalCount;
	@Nullable
	private BlockPos partnerPos;
	private int linkDistance;
	private boolean portalBlocksActive;
	@Nullable
	private VoidPortalShape lastFilledShape;

	public VoidPortalConnectorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						blockState -> blockState.getValue(VoidPortalConnectorBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));
		link = new VoidPortalLinkBehaviour(this, slots);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	public VoidPortalLinkBehaviour getPortalLink() {
		return link;
	}

	@Nullable
	public VoidPortalShape getCachedShape() {
		return cachedShape;
	}

	@Override
	public void setNetworkState(VoidPortalNetworkHandler.PairStatus status, int portalCount,
			@Nullable BlockPos partner, int linkDistance) {
		this.pairStatus = status;
		this.portalCount = portalCount;
		this.partnerPos = partner;
		this.linkDistance = linkDistance;
		sendData();
	}

	@Override
	public VoidPortalNetworkHandler.PairStatus getPairStatus() {
		return pairStatus;
	}

	@Override
	public int getPortalCount() {
		return portalCount;
	}

	@Nullable
	@Override
	public BlockPos getPartnerPos() {
		return partnerPos;
	}

	@Override
	public int getLinkDistance() {
		return linkDistance;
	}

	public int getLinkStressDemand() {
		if (pairStatus != VoidPortalNetworkHandler.PairStatus.VALID)
			return 0;
		return VoidPortalLinkMetrics.computeStressDemand(linkDistance);
	}

	public boolean hasValidFrame() {
		return getActiveShape() != null;
	}

	@Nullable
	public VoidPortalShape getActiveShape() {
		if (cachedShape != null)
			return cachedShape;
		if (level != null)
			return VoidPortalShape.findAt(level, worldPosition);
		return null;
	}

	public boolean canOperate() {
		return shouldActivatePortalBlocks();
	}

	public boolean shouldActivatePortalBlocks() {
		return getActiveShape() != null
				&& hasFrequencyConfigured()
				&& pairStatus == VoidPortalNetworkHandler.PairStatus.VALID
				&& hasRequiredStress()
				&& hasTransferFluid();
	}

	public boolean hasTransferFluid() {
		VoidPortalFluidTileEntity fluid = getFluidTile();
		return fluid != null && fluid.getFluidTank().getFluidAmount() > 0;
	}

	public boolean hasFrequencyConfigured() {
		return VoidPortalNetworkHandler.hasFrequencyConfigured(link);
	}

	public boolean hasRequiredStress() {
		VoidPortalShape shape = getActiveShape();
		if (shape == null || level == null)
			return false;
		if (!(level.getBlockEntity(shape.stressPos()) instanceof VoidPortalStressTileEntity stress))
			return false;
		return stress.hasRequiredStress();
	}

	@Nullable
	private VoidPortalFluidTileEntity getFluidTile() {
		VoidPortalShape shape = getActiveShape();
		if (shape == null || level == null)
			return null;
		if (level.getBlockEntity(shape.fluidPos()) instanceof VoidPortalFluidTileEntity fluid)
			return fluid;
		return null;
	}

	public void refreshShapeAndNetwork() {
		if (level == null || level.isClientSide)
			return;
		VoidPortalShape previous = cachedShape;
		updateCachedShape();
		if (previous != null && cachedShape == null)
			clearPortalBlocks((ServerLevel) level, previous);
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.refreshPortal(level, worldPosition);
		refreshPortalBlocks();
		setChanged();
		sendData();
	}

	void updateCachedShape() {
		if (level == null)
			cachedShape = null;
		else
			cachedShape = VoidPortalShape.findAt(level, worldPosition);
	}

	public void refreshPortalBlocks() {
		if (level == null || level.isClientSide)
			return;
		ServerLevel serverLevel = (ServerLevel) level;
		boolean should = shouldActivatePortalBlocks();
		if (cachedShape == null) {
			if (portalBlocksActive && lastFilledShape != null)
				clearPortalBlocks(serverLevel, lastFilledShape);
			portalBlocksActive = false;
			lastFilledShape = null;
			return;
		}
		if (should) {
			VoidPortalBlockSync.fill(serverLevel, cachedShape);
			portalBlocksActive = true;
			lastFilledShape = cachedShape;
		} else if (portalBlocksActive) {
			clearPortalBlocks(serverLevel, cachedShape);
			portalBlocksActive = false;
			lastFilledShape = null;
		}
	}

	private void clearPortalBlocks(ServerLevel level, VoidPortalShape shape) {
		VoidPortalBlockSync.clear(level, shape);
	}

	public void clearPortalBlocksOnRemove() {
		if (level == null || level.isClientSide)
			return;
		if (lastFilledShape != null)
			clearPortalBlocks((ServerLevel) level, lastFilledShape);
		else if (cachedShape != null)
			clearPortalBlocks((ServerLevel) level, cachedShape);
		portalBlocksActive = false;
		lastFilledShape = null;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide)
			refreshShapeAndNetwork();
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		refreshShapeAndNetwork();
		VoidwayMod.VOID_PORTAL_NETWORK_HANDLER.tickCooldowns((ServerLevel) level);
		processEntities((ServerLevel) level);
	}

	private void processEntities(ServerLevel serverLevel) {
		if (!canOperate() || cachedShape == null || !portalBlocksActive)
			return;

		VoidPortalFluidTileEntity fluidTe = getFluidTile();
		if (fluidTe == null)
			return;

		VoidPortalShape destShape = getPartnerShape(serverLevel);
		if (destShape == null)
			return;

		List<Entity> candidates = serverLevel.getEntities((Entity) null, cachedShape.getInteriorBounds(),
				entity -> VoidPortalHelper.canBeTeleported(entity)
						&& !VoidPortalHelper.hasContactCooldown(entity));

		for (Entity entity : candidates) {
			if (!VoidPortalHelper.isEntityTouchingPortalBlock(serverLevel, cachedShape, entity)) {
				VoidPortalHelper.clearPortalCharge(entity);
				continue;
			}

			int charge = VoidPortalHelper.getPortalChargeTicks(entity) + 1;
			if (charge < VoidPortalHelper.TELEPORT_DELAY_TICKS) {
				VoidPortalHelper.setPortalChargeTicks(entity, charge);
				continue;
			}

			if (tryTeleportEntity(serverLevel, entity, fluidTe, destShape)) {
				serverLevel.sendParticles(ParticleTypes.PORTAL,
						entity.getX(), entity.getY(0.5), entity.getZ(),
						16, 0.2, 0.4, 0.2, 0.05);
				VoidPortalHelper.clearPortalCharge(entity);
			}
		}
	}

	@Nullable
	private VoidPortalShape getPartnerShape(ServerLevel level) {
		if (partnerPos == null || !level.isLoaded(partnerPos))
			return null;
		if (!(level.getBlockEntity(partnerPos) instanceof VoidPortalConnectorTileEntity partner))
			return null;
		if (partner.getPairStatus() != VoidPortalNetworkHandler.PairStatus.VALID)
			return null;
		return partner.getCachedShape();
	}

	private boolean tryTeleportEntity(ServerLevel level, Entity entity, VoidPortalFluidTileEntity fluidTe,
			VoidPortalShape destShape) {
		if (!entity.isAlive() || cachedShape == null)
			return false;
		if (!VoidPortalHelper.isEntityTouchingPortalBlock(level, cachedShape, entity))
			return false;

		int cost = VoidPortalHelper.getFluidCostFor(entity);
		if (fluidTe.getFluidTank().getFluidAmount() < cost)
			return false;
		if (fluidTe.getFluidTank().drain(cost, IFluidHandler.FluidAction.SIMULATE).getAmount() < cost)
			return false;

		fluidTe.getFluidTank().drain(cost, IFluidHandler.FluidAction.EXECUTE);
		fluidTe.setChanged();
		fluidTe.sendData();
		VoidPortalHelper.teleportTo(level, entity, destShape);
		setChanged();
		return true;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		pairStatus = VoidPortalNetworkHandler.PairStatus.values()[
				Math.min(tag.getInt("PairStatus"), VoidPortalNetworkHandler.PairStatus.values().length - 1)];
		portalCount = tag.getInt("PortalCount");
		if (tag.contains("PartnerPos"))
			partnerPos = BlockPos.of(tag.getLong("PartnerPos"));
		else
			partnerPos = null;
		linkDistance = tag.getInt("LinkDistance");
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.putInt("PairStatus", pairStatus.ordinal());
		tag.putInt("PortalCount", portalCount);
		if (partnerPos != null)
			tag.putLong("PartnerPos", partnerPos.asLong());
		tag.putInt("LinkDistance", linkDistance);
		super.write(tag, registries, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidPortalGoggleTooltip.addConnector(tooltip, this);
		return true;
	}

}

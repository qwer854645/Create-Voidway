package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.xeli.createvoidway.config.VoidChannelStress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class VoidPortalStressTileEntity extends KineticBlockEntity implements IHaveGoggleInformation {

	public VoidPortalStressTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public boolean hasShaftConnection() {
		if (level == null)
			return false;
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof VoidPortalStressBlock block))
			return false;
		Direction front = state.getValue(VoidPortalStressBlock.FACING);
		for (Direction direction : new Direction[] { front, front.getOpposite() }) {
			if (!block.hasShaftTowards(level, worldPosition, state, direction))
				continue;
			BlockPos neighborPos = worldPosition.relative(direction);
			BlockState neighborState = level.getBlockState(neighborPos);
			if (neighborState.getBlock() instanceof IRotate neighborRotate
					&& neighborRotate.hasShaftTowards(level, neighborPos, neighborState, direction.getOpposite()))
				return true;
		}
		return false;
	}

	public boolean hasRequiredStress() {
		return hasShaftConnection() && hasSource() && !isOverStressed()
				&& Math.abs(getTheoreticalSpeed()) > 0;
	}

	public int getStressDemand(int linkDistance) {
		return VoidPortalLinkMetrics.computeStressDemand(linkDistance);
	}

	@Override
	public float calculateStressApplied() {
		if (!hasShaftConnection() || Math.abs(getTheoreticalSpeed()) == 0)
			return 0;
		if (level == null)
			return 0;
		VoidPortalShape shape = VoidPortalShape.findAt(level, worldPosition);
		if (shape == null)
			return 0;
		if (!(level.getBlockEntity(shape.connectorPos()) instanceof VoidPortalConnectorTileEntity connector))
			return 0;
		if (connector.getPairStatus() != VoidPortalNetworkHandler.PairStatus.VALID)
			return 0;
		return VoidChannelStress.toImpactPerRpm(getStressDemand(connector.getLinkDistance()), getTheoreticalSpeed());
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidPortalGoggleTooltip.addStress(tooltip, this);
		return true;
	}

}

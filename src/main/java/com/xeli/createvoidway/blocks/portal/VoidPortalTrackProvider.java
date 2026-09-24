package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.xeli.createvoidway.blocks.RWBlocks;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Lets Create trains link tracks through paired void portals, mirroring {@code AllPortalTracks} for the nether portal.
 */
public final class VoidPortalTrackProvider implements PortalTrackProvider {

	@Override
	@Nullable
	public Exit findExit(ServerLevel level, BlockFace inboundTrack) {
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);
		if (!portalState.is(RWBlocks.VOID_PORTAL.get()))
			return null;

		VoidPortalShape sourceShape = VoidPortalShape.findAtPortalBlock(level, portalPos);
		if (sourceShape == null)
			return null;

		if (!(level.getBlockEntity(sourceShape.connectorPos()) instanceof VoidPortalConnectorTileEntity sourceConnector))
			return null;
		if (sourceConnector.getPairStatus() != VoidPortalNetworkHandler.PairStatus.VALID)
			return null;
		if (sourceConnector.getPartnerPos() == null)
			return null;

		VoidPortalConnectorTileEntity destConnector = sourceConnector.resolvePartner(true);
		if (destConnector == null)
			return null;
		if (!(destConnector.getLevel() instanceof ServerLevel destLevel))
			return null;

		BlockPos partnerPos = sourceConnector.getPartnerPos();
		destConnector.updateCachedShape();

		VoidPortalShape destShape = destConnector.getCachedShape();
		if (destShape == null)
			destShape = VoidPortalShape.findAt(destLevel, partnerPos);
		if (destShape == null)
			return null;

		BlockPos destPortalPos = VoidPortalShape.mapPortalBlock(sourceShape, portalPos, destShape);
		if (destPortalPos == null)
			return null;

		// Never call refreshPortal/refreshPortalBlocks here — those can CLEAR dest portal blocks
		// when pair state is briefly stale after force-load, which breaks track linking.
		if (!destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			destConnector.ensurePortalBlocksFilled();
		if (!destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			destConnector.forceFillPortalBlocksForTracks();
		if (!destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			return null;

		// Match Create nether portals: exit facing uses the destination portal axis.
		BlockState destPortalState = destLevel.getBlockState(destPortalPos);
		Direction.Axis portalAxis = destPortalState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
				? destPortalState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
				: portalState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
		Direction targetDirection = inboundTrack.getFace();
		if (targetDirection.getAxis() == portalAxis)
			targetDirection = targetDirection.getClockWise();

		BlockPos exitTrackPos = destPortalPos.relative(targetDirection);
		// If the natural exit is blocked, try the opposite side of the portal plane.
		if (!destLevel.getBlockState(exitTrackPos).canBeReplaced()) {
			Direction opposite = targetDirection.getOpposite();
			if (opposite.getAxis() != portalAxis) {
				BlockPos alt = destPortalPos.relative(opposite);
				if (destLevel.getBlockState(alt).canBeReplaced()) {
					targetDirection = opposite;
					exitTrackPos = alt;
				}
			}
		}

		return new Exit(destLevel, new BlockFace(exitTrackPos, targetDirection.getOpposite()));
	}

	public static void register() {
		PortalTrackProvider.REGISTRY.register(RWBlocks.VOID_PORTAL.get(), new VoidPortalTrackProvider());
	}

}

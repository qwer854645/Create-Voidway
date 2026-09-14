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

		VoidPortalConnectorTileEntity destConnector = sourceConnector.resolvePartner(true);
		if (destConnector == null)
			return null;
		if (!(destConnector.getLevel() instanceof ServerLevel destLevel))
			return null;
		if (!destConnector.isLocallyReady())
			return null;

		// Force-loaded destinations may not have ticked yet — fill portal blocks now.
		destConnector.refreshPortalBlocks();

		BlockPos partnerPos = sourceConnector.getPartnerPos();
		if (partnerPos == null)
			return null;

		VoidPortalShape destShape = destConnector.getCachedShape();
		if (destShape == null)
			destShape = VoidPortalShape.findAt(destLevel, partnerPos);
		if (destShape == null)
			return null;

		BlockPos destPortalPos = VoidPortalShape.mapPortalBlock(sourceShape, portalPos, destShape);
		if (destPortalPos == null || !destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
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
		return new Exit(destLevel, new BlockFace(exitTrackPos, targetDirection.getOpposite()));
	}

	public static void register() {
		PortalTrackProvider.REGISTRY.register(RWBlocks.VOID_PORTAL.get(), new VoidPortalTrackProvider());
	}

}

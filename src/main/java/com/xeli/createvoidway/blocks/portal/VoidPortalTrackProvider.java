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
		if (!sourceConnector.shouldActivatePortalBlocks())
			return null;

		BlockPos partnerPos = sourceConnector.getPartnerPos();
		if (partnerPos == null)
			return null;
		if (!(level.getBlockEntity(partnerPos) instanceof VoidPortalConnectorTileEntity destConnector))
			return null;
		if (!destConnector.shouldActivatePortalBlocks())
			return null;

		VoidPortalShape destShape = destConnector.getCachedShape();
		if (destShape == null)
			destShape = VoidPortalShape.findAt(level, partnerPos);
		if (destShape == null)
			return null;

		BlockPos destPortalPos = VoidPortalShape.mapPortalBlock(sourceShape, portalPos, destShape);
		if (destPortalPos == null || !level.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			return null;

		Direction.Axis portalAxis = portalState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
		Direction targetDirection = inboundTrack.getFace();
		if (targetDirection.getAxis() == portalAxis)
			targetDirection = targetDirection.getClockWise();

		BlockPos exitTrackPos = destPortalPos.relative(targetDirection);
		return new Exit(level, new BlockFace(exitTrackPos, targetDirection.getOpposite()));
	}

	public static void register() {
		PortalTrackProvider.REGISTRY.register(RWBlocks.VOID_PORTAL.get(), new VoidPortalTrackProvider());
	}

}

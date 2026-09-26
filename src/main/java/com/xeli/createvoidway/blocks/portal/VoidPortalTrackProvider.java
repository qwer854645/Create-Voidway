package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.xeli.createvoidway.VoidwayMod;
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
			return fail(level, portalPos, "not_void_portal_block");

		VoidPortalShape sourceShape = VoidPortalShape.findAtPortalBlock(level, portalPos);
		if (sourceShape == null)
			return fail(level, portalPos, "source_shape_missing");

		if (!(level.getBlockEntity(sourceShape.connectorPos()) instanceof VoidPortalConnectorTileEntity sourceConnector))
			return fail(level, portalPos, "source_connector_missing");
		if (sourceConnector.getPairStatus() != VoidPortalNetworkHandler.PairStatus.VALID)
			return fail(level, portalPos, "source_pair_not_valid status=" + sourceConnector.getPairStatus()
					+ " count=" + sourceConnector.getPortalCount());
		if (sourceConnector.getPartnerPos() == null)
			return fail(level, portalPos, "source_partner_pos_null");

		VoidPortalConnectorTileEntity destConnector = sourceConnector.resolvePartner(true);
		if (destConnector == null)
			return fail(level, portalPos, "resolve_partner_failed partnerPos=" + sourceConnector.getPartnerPos()
					+ " partnerDim=" + sourceConnector.getPartnerDimension());
		if (!(destConnector.getLevel() instanceof ServerLevel destLevel))
			return fail(level, portalPos, "dest_level_not_server");

		BlockPos partnerPos = sourceConnector.getPartnerPos();
		destConnector.updateCachedShape();

		VoidPortalShape destShape = destConnector.getCachedShape();
		if (destShape == null)
			destShape = VoidPortalShape.findAt(destLevel, partnerPos);
		if (destShape == null)
			return fail(level, portalPos, "dest_shape_missing at " + partnerPos + " in " + destLevel.dimension().location());

		BlockPos destPortalPos = VoidPortalShape.mapPortalBlock(sourceShape, portalPos, destShape);
		if (destPortalPos == null)
			return fail(level, portalPos, "map_portal_block_failed srcInner="
					+ sourceShape.innerWidth() + "x" + sourceShape.innerHeight()
					+ " destInner=" + destShape.innerWidth() + "x" + destShape.innerHeight()
					+ " srcW=[" + (sourceShape.left() + 1) + ".." + (sourceShape.right() - 1) + "]"
					+ " srcY=[" + (sourceShape.bottom() + 1) + ".." + (sourceShape.top() - 1) + "]"
					+ " destW=[" + (destShape.left() + 1) + ".." + (destShape.right() - 1) + "]"
					+ " destY=[" + (destShape.bottom() + 1) + ".." + (destShape.top() - 1) + "]"
					+ " portal=" + portalPos);

		if (!destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			destConnector.ensurePortalBlocksFilled();
		if (!destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			destConnector.forceFillPortalBlocksForTracks();
		if (!destLevel.getBlockState(destPortalPos).is(RWBlocks.VOID_PORTAL.get()))
			return fail(level, portalPos, "dest_portal_block_missing_after_fill at " + destPortalPos
					+ " in " + destLevel.dimension().location()
					+ " state=" + destLevel.getBlockState(destPortalPos));

		BlockState destPortalState = destLevel.getBlockState(destPortalPos);
		Direction.Axis portalAxis = destPortalState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
				? destPortalState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
				: portalState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
		Direction targetDirection = inboundTrack.getFace();
		if (targetDirection.getAxis() == portalAxis)
			targetDirection = targetDirection.getClockWise();

		BlockPos exitTrackPos = destPortalPos.relative(targetDirection);
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

		if (!destLevel.getBlockState(exitTrackPos).canBeReplaced())
			return fail(level, portalPos, "exit_blocked at " + exitTrackPos
					+ " in " + destLevel.dimension().location()
					+ " state=" + destLevel.getBlockState(exitTrackPos));

		VoidwayMod.LOGGER.info("[VoidPortalTrack] linked {} {} -> {} {} exit={}",
				level.dimension().location(), portalPos,
				destLevel.dimension().location(), destPortalPos, exitTrackPos);
		return new Exit(destLevel, new BlockFace(exitTrackPos, targetDirection.getOpposite()));
	}

	private static Exit fail(ServerLevel level, BlockPos portalPos, String reason) {
		VoidwayMod.LOGGER.warn("[VoidPortalTrack] findExit failed at {} in {}: {}",
				portalPos, level.dimension().location(), reason);
		return null;
	}

	public static void register() {
		PortalTrackProvider.REGISTRY.register(RWBlocks.VOID_PORTAL.get(), new VoidPortalTrackProvider());
		VoidwayMod.LOGGER.info("[VoidPortalTrack] registered provider for {}", RWBlocks.VOID_PORTAL.getId());
	}

}

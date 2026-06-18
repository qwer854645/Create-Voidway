package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.RWBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Axis-aligned hollow square portal frame (vanilla-portal style).
 * The bottom row must contain exactly one fluid port, connector, and stress port in any order;
 * everything else is frame. The walkable opening must be square (inner width equals inner height).
 */
public record VoidPortalShape(
		Direction.Axis walkAxis,
		BlockPos connectorPos,
		BlockPos fluidPos,
		BlockPos stressPos,
		int left,
		int right,
		int bottom,
		int top,
		int planeCoord) {

	public static final int MIN_INNER_WIDTH = 3;
	/** Walkable opening height in blocks (not counting the bottom interface row). */
	public static final int MIN_INNER_HEIGHT = 2;
	public static final int MAX_INNER_WIDTH = 21;
	public static final int MAX_INNER_HEIGHT = 21;

	@Nullable
	public static VoidPortalShape findAt(LevelAccessor level, BlockPos pos) {
		return findAt(level, pos, true);
	}

	@Nullable
	public static VoidPortalShape findAtPortalBlock(LevelAccessor level, BlockPos pos) {
		if (!level.getBlockState(pos).is(RWBlocks.VOID_PORTAL.get()))
			return null;
		for (int dy = 0; dy <= MAX_INNER_HEIGHT + 2; dy++) {
			BlockPos below = pos.below(dy);
			if (!isBottomInterfaceBlock(level, below))
				continue;
			VoidPortalShape shape = findFromBottomInterface(level, below, true);
			if (shape != null && shape.containsInteriorBlock(pos))
				return shape;
		}
		return findNearBottomInterface(level, pos, true);
	}

	/**
	 * @param requireSquare when true, inner width and walkable height must match
	 */
	@Nullable
	public static VoidPortalShape findAt(LevelAccessor level, BlockPos pos, boolean requireSquare) {
		BlockState state = level.getBlockState(pos);
		if (state.is(RWBlocks.VOID_PORTAL.get()))
			return findAtPortalBlock(level, pos);
		if (state.is(RWBlocks.VOID_PORTAL_CONNECTOR.get()))
			return findFromConnector(level, pos, requireSquare);
		if (state.is(RWBlocks.VOID_PORTAL_FLUID.get()) || state.is(RWBlocks.VOID_PORTAL_STRESS.get()))
			return findFromBottomInterface(level, pos, requireSquare);
		if (state.is(RWBlocks.VOID_PORTAL_FRAME.get()))
			return findNearConnector(level, pos, requireSquare);
		return null;
	}

	/** Returns [innerWidth, innerHeight] when the frame is complete but not square; otherwise null. */
	@Nullable
	public static int[] getNonSquareOpeningDimensions(LevelAccessor level, BlockPos pos) {
		VoidPortalShape relaxed = findAt(level, pos, false);
		if (relaxed == null)
			return null;
		if (relaxed.innerWidth() == relaxed.innerHeight())
			return null;
		return new int[] { relaxed.innerWidth(), relaxed.innerHeight() };
	}

	@Nullable
	private static VoidPortalShape findNearConnector(LevelAccessor level, BlockPos pos, boolean requireSquare) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dz = -2; dz <= 2; dz++) {
					BlockPos check = pos.offset(dx, dy, dz);
					if (level.getBlockState(check).is(RWBlocks.VOID_PORTAL_CONNECTOR.get())) {
						VoidPortalShape shape = findFromConnector(level, check, requireSquare);
						if (shape != null)
							return shape;
					}
				}
			}
		}
		return findNearBottomInterface(level, pos, requireSquare);
	}

	@Nullable
	private static VoidPortalShape findNearBottomInterface(LevelAccessor level, BlockPos pos, boolean requireSquare) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dz = -2; dz <= 2; dz++) {
					BlockPos check = pos.offset(dx, dy, dz);
					BlockState state = level.getBlockState(check);
					if (state.is(RWBlocks.VOID_PORTAL_FLUID.get()) || state.is(RWBlocks.VOID_PORTAL_STRESS.get())
							|| state.is(RWBlocks.VOID_PORTAL_CONNECTOR.get())) {
						VoidPortalShape shape = state.is(RWBlocks.VOID_PORTAL_CONNECTOR.get())
								? findFromConnector(level, check, requireSquare)
								: findFromBottomInterface(level, check, requireSquare);
						if (shape != null)
							return shape;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static VoidPortalShape findFromBottomInterface(LevelAccessor level, BlockPos anchorPos, boolean requireSquare) {
		if (!isBottomInterfaceBlock(level, anchorPos))
			return null;

		Direction.Axis preferredWidthAxis = preferredWidthAxis(level, anchorPos);
		Direction.Axis alternateWidthAxis = preferredWidthAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

		VoidPortalShape shape = validateFrame(level, anchorPos, null, preferredWidthAxis, requireSquare);
		if (shape != null)
			return shape;
		return validateFrame(level, anchorPos, null, alternateWidthAxis, requireSquare);
	}

	@Nullable
	private static VoidPortalShape findFromConnector(LevelAccessor level, BlockPos connectorPos, boolean requireSquare) {
		if (!level.getBlockState(connectorPos).is(RWBlocks.VOID_PORTAL_CONNECTOR.get()))
			return null;

		Direction.Axis preferredWidthAxis = preferredWidthAxis(level, connectorPos);
		Direction.Axis alternateWidthAxis = preferredWidthAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

		VoidPortalShape shape = validateFrame(level, connectorPos, connectorPos, preferredWidthAxis, requireSquare);
		if (shape != null)
			return shape;
		return validateFrame(level, connectorPos, connectorPos, alternateWidthAxis, requireSquare);
	}

	private static Direction.Axis preferredWidthAxis(LevelAccessor level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.hasProperty(VoidPortalConnectorBlock.FACING)) {
			Direction facing = state.getValue(VoidPortalConnectorBlock.FACING);
			if (facing.getAxis().isHorizontal())
				return facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		}
		return Direction.Axis.X;
	}

	@Nullable
	private static VoidPortalShape validateFrame(LevelAccessor level, BlockPos anchorPos,
			@Nullable BlockPos requiredConnector, Direction.Axis widthAxis, boolean requireSquare) {
		if (!isBottomInterfaceBlock(level, anchorPos))
			return null;

		int bottom = anchorPos.getY();
		int planeCoord = widthAxis == Direction.Axis.X ? anchorPos.getZ() : anchorPos.getX();
		if (!isOnBottomPlane(level, anchorPos, widthAxis, planeCoord, bottom))
			return null;

		int left = getCoord(anchorPos, widthAxis);
		int right = left;

		while (isBottomInterfaceAt(level, widthAxis, planeCoord, left - 1, bottom))
			left--;
		while (isBottomInterfaceAt(level, widthAxis, planeCoord, right + 1, bottom))
			right++;

		while (isFrameAt(level, widthAxis, planeCoord, left - 1, bottom))
			left--;
		while (isFrameAt(level, widthAxis, planeCoord, right + 1, bottom))
			right++;

		int innerWidth = right - left - 1;
		if (innerWidth < MIN_INNER_WIDTH || innerWidth > MAX_INNER_WIDTH)
			return null;

		if (!isFrameAt(level, widthAxis, planeCoord, left, bottom)
				|| !isFrameAt(level, widthAxis, planeCoord, right, bottom))
			return null;

		BlockPos fluidPos = null;
		BlockPos connectorPos = null;
		BlockPos stressPos = null;

		for (int w = left + 1; w < right; w++) {
			BlockPos pos = posAt(widthAxis, planeCoord, w, bottom);
			if (isFluid(level, pos)) {
				if (fluidPos != null)
					return null;
				fluidPos = pos;
			} else if (isConnector(level, pos)) {
				if (connectorPos != null)
					return null;
				connectorPos = pos;
			} else if (isStress(level, pos)) {
				if (stressPos != null)
					return null;
				stressPos = pos;
			} else if (!isFrame(level, pos)) {
				return null;
			}
		}

		if (fluidPos == null || connectorPos == null || stressPos == null)
			return null;
		if (requiredConnector != null && !connectorPos.equals(requiredConnector))
			return null;

		int top = bottom;
		while (isFrameAt(level, widthAxis, planeCoord, left, top + 1)
				&& isFrameAt(level, widthAxis, planeCoord, right, top + 1))
			top++;

		int walkableHeight = top - bottom - 1;
		if (walkableHeight < MIN_INNER_HEIGHT || walkableHeight > MAX_INNER_HEIGHT)
			return null;

		if (requireSquare && innerWidth != walkableHeight)
			return null;

		for (int w = left; w <= right; w++) {
			if (!isFrameAt(level, widthAxis, planeCoord, w, top))
				return null;
		}

		for (int y = bottom + 1; y < top; y++) {
			if (!isFrameAt(level, widthAxis, planeCoord, left, y)
					|| !isFrameAt(level, widthAxis, planeCoord, right, y))
				return null;
		}

		for (int y = bottom + 1; y < top; y++) {
			for (int w = left + 1; w < right; w++) {
				BlockPos interior = posAt(widthAxis, planeCoord, w, y);
				if (!isPortalPassable(level, interior))
					return null;
			}
		}

		Direction.Axis walkAxis = widthAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		return new VoidPortalShape(walkAxis, connectorPos, fluidPos, stressPos, left, right, bottom, top, planeCoord);
	}

	public int innerWidth() {
		return right - left - 1;
	}

	public int innerHeight() {
		return top - bottom - 1;
	}

	public Direction.Axis widthAxis() {
		return walkAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
	}

	public boolean containsInteriorBlock(BlockPos pos) {
		int w = getCoord(pos, widthAxis());
		if (w <= left || w >= right || pos.getY() <= bottom || pos.getY() >= top)
			return false;
		return walkAxis == Direction.Axis.Z ? pos.getZ() == planeCoord : pos.getX() == planeCoord;
	}

	@Nullable
	public static BlockPos mapPortalBlock(VoidPortalShape source, BlockPos sourcePortal, VoidPortalShape dest) {
		Direction.Axis widthAxis = source.widthAxis();
		int destW = mapInteriorCoord(source.left() + 1, source.right() - 1, dest.left() + 1, dest.right() - 1,
				getCoord(sourcePortal, widthAxis));
		if (destW < 0)
			return null;
		int destY = mapInteriorCoord(source.bottom() + 1, source.top() - 1, dest.bottom() + 1, dest.top() - 1,
				sourcePortal.getY());
		if (destY < 0)
			return null;
		return posAt(widthAxis, dest.planeCoord(), destW, destY);
	}

	private static int mapInteriorCoord(int sourceMin, int sourceMax, int destMin, int destMax, int sourceCoord) {
		if (sourceCoord < sourceMin || sourceCoord > sourceMax)
			return -1;
		int sourceSpan = sourceMax - sourceMin;
		int destSpan = destMax - destMin;
		if (sourceSpan <= 0 && destSpan <= 0)
			return sourceCoord == sourceMin ? destMin : -1;
		if (sourceSpan <= 0)
			return destMin + destSpan / 2;
		int rel = sourceCoord - sourceMin;
		return destMin + (rel * destSpan) / sourceSpan;
	}

	public AABB getInteriorBounds() {
		double minW = left + 1;
		double maxW = right;
		double minY = bottom + 1;
		double maxY = top;
		if (walkAxis == Direction.Axis.Z) {
			return new AABB(minW, minY, planeCoord, maxW, maxY, planeCoord + 1);
		}
		return new AABB(planeCoord, minY, minW, planeCoord + 1, maxY, maxW);
	}

	public BlockPos getSpawnPos() {
		int centerW = (left + right) / 2;
		Direction.Axis widthAxis = walkAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
		return posAt(widthAxis, planeCoord, centerW, bottom + 1);
	}

	public boolean containsEntity(net.minecraft.world.entity.Entity entity) {
		return getInteriorBounds().intersects(entity.getBoundingBox());
	}

	public Iterable<BlockPos> iterateInteriorPositions() {
		Direction.Axis widthAxis = walkAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
		List<BlockPos> positions = new ArrayList<>();
		for (int y = bottom + 1; y < top; y++) {
			for (int w = left + 1; w < right; w++)
				positions.add(posAt(widthAxis, planeCoord, w, y));
		}
		return positions;
	}

	public static boolean isReplaceableByPortal(LevelAccessor level, BlockPos pos) {
		return isPortalPassable(level, pos);
	}

	public static Optional<VoidPortalShape> optionalAt(LevelAccessor level, BlockPos pos) {
		return Optional.ofNullable(findAt(level, pos));
	}

	private static boolean isBottomInterfaceBlock(LevelAccessor level, BlockPos pos) {
		return isFluid(level, pos) || isStress(level, pos) || isConnector(level, pos);
	}

	private static boolean isOnBottomPlane(LevelAccessor level, BlockPos pos, Direction.Axis widthAxis, int planeCoord,
			int bottom) {
		if (pos.getY() != bottom)
			return false;
		return widthAxis == Direction.Axis.X ? pos.getZ() == planeCoord : pos.getX() == planeCoord;
	}

	private static boolean isBottomInterfaceAt(LevelAccessor level, Direction.Axis widthAxis, int planeCoord, int widthCoord,
			int bottom) {
		return isBottomInterfaceBlock(level, posAt(widthAxis, planeCoord, widthCoord, bottom));
	}

	private static int getCoord(BlockPos pos, Direction.Axis widthAxis) {
		return widthAxis == Direction.Axis.X ? pos.getX() : pos.getZ();
	}

	private static BlockPos posAt(Direction.Axis widthAxis, int planeCoord, int widthCoord, int y) {
		if (widthAxis == Direction.Axis.X)
			return new BlockPos(widthCoord, y, planeCoord);
		return new BlockPos(planeCoord, y, widthCoord);
	}

	private static boolean isFrameAt(LevelAccessor level, Direction.Axis widthAxis, int planeCoord, int widthCoord, int y) {
		return isFrame(level, posAt(widthAxis, planeCoord, widthCoord, y));
	}

	private static boolean isFrame(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos).is(RWBlocks.VOID_PORTAL_FRAME.get());
	}

	private static boolean isFluid(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos).is(RWBlocks.VOID_PORTAL_FLUID.get());
	}

	private static boolean isStress(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos).is(RWBlocks.VOID_PORTAL_STRESS.get());
	}

	private static boolean isConnector(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos).is(RWBlocks.VOID_PORTAL_CONNECTOR.get());
	}

	private static boolean isPortalPassable(LevelAccessor level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(RWBlocks.VOID_PORTAL.get()))
			return true;
		if (state.is(RWBlocks.VOID_PORTAL_FRAME.get()))
			return false;
		if (state.is(RWBlocks.VOID_PORTAL_FLUID.get()) || state.is(RWBlocks.VOID_PORTAL_STRESS.get())
				|| state.is(RWBlocks.VOID_PORTAL_CONNECTOR.get()))
			return false;
		return state.isAir() || state.getFluidState().getType() == Fluids.WATER
				|| state.getFluidState().getType() == Fluids.FLOWING_WATER;
	}

}

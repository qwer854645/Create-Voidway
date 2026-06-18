package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class VoidPortalStressVisual extends KineticBlockEntityVisual<VoidPortalStressTileEntity> {

	protected final EnumMap<Direction, RotatingInstance> shafts = new EnumMap<>(Direction.class);
	@Nullable
	protected Direction sourceFacing;

	public VoidPortalStressVisual(VisualizationContext context, VoidPortalStressTileEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		updateSourceFacing();

		Instancer<RotatingInstance> instancer = instancerProvider()
				.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));

		Direction.Axis axis = blockEntity.getBlockState().getValue(VoidPortalStressBlock.FACING).getAxis();
		for (Direction direction : getShaftFaces()) {
			RotatingInstance shaft = instancer.createInstance();
			shaft.setup(blockEntity, axis, getSpeed(direction))
					.setPosition(getShaftPosition(direction))
					.rotateToFace(Direction.SOUTH, direction)
					.setChanged();
			shafts.put(direction, shaft);
		}
	}

	private Direction[] getShaftFaces() {
		Direction front = blockEntity.getBlockState().getValue(VoidPortalStressBlock.FACING);
		return new Direction[] { front, front.getOpposite() };
	}

	private float getSpeed(Direction direction) {
		float speed = blockEntity.getSpeed();
		if (speed != 0 && sourceFacing != null) {
			if (sourceFacing.getAxis() == direction.getAxis())
				speed *= sourceFacing == direction ? 1 : -1;
			else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
				speed *= -1;
		}
		return speed;
	}

	protected Vector3f getShaftPosition(Direction face) {
		return VoidPortalStressShaftGeometry.flywheelPosition(getVisualPosition(), face);
	}

	protected void updateSourceFacing() {
		if (blockEntity.hasSource()) {
			var source = blockEntity.source.subtract(pos);
			sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
		} else
			sourceFacing = null;
	}

	@Override
	public void update(float partialTick) {
		updateSourceFacing();
		Direction.Axis axis = blockEntity.getBlockState().getValue(VoidPortalStressBlock.FACING).getAxis();
		for (Map.Entry<Direction, RotatingInstance> entry : shafts.entrySet()) {
			Direction direction = entry.getKey();
			entry.getValue().setup(blockEntity, axis, getSpeed(direction))
					.setPosition(getShaftPosition(direction))
					.setChanged();
		}
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		shafts.values().forEach(consumer);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(shafts.values().toArray(FlatLit[]::new));
	}

	@Override
	protected void _delete() {
		shafts.values().forEach(AbstractInstance::delete);
		shafts.clear();
	}

}

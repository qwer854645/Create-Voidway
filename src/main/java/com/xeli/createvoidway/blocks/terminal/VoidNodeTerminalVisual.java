package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class VoidNodeTerminalVisual extends KineticBlockEntityVisual<VoidNodeTerminalTileEntity> {

	private static final Direction SHAFT_FACE = Direction.DOWN;

	protected RotatingInstance shaft;
	@Nullable
	protected Direction sourceFacing;

	public VoidNodeTerminalVisual(VisualizationContext context, VoidNodeTerminalTileEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		updateSourceFacing();
		Instancer<RotatingInstance> instancer = instancerProvider()
				.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));
		shaft = instancer.createInstance();
		shaft.setup(blockEntity, Direction.Axis.Y, getSpeed())
				.setPosition(getShaftPosition())
				.rotateToFace(Direction.SOUTH, SHAFT_FACE)
				.setChanged();
	}

	protected Vector3f getShaftPosition() {
		BlockPos visualPos = getVisualPosition();
		return new Vector3f(visualPos.getX(), visualPos.getY() + VoidShaftBuffers.padTerminalShaftDownOffset(),
				visualPos.getZ());
	}

	private float getSpeed() {
		float speed = blockEntity.getSpeed();
		if (speed != 0 && sourceFacing != null) {
			if (sourceFacing.getAxis() == SHAFT_FACE.getAxis())
				speed *= sourceFacing == SHAFT_FACE ? 1 : -1;
			else if (sourceFacing.getAxisDirection() == SHAFT_FACE.getAxisDirection())
				speed *= -1;
		}
		return speed;
	}

	protected void updateSourceFacing() {
		if (blockEntity.hasSource()) {
			BlockPos source = blockEntity.source.subtract(pos);
			sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
		} else
			sourceFacing = null;
	}

	@Override
	public void update(float partialTick) {
		updateSourceFacing();
		shaft.setup(blockEntity, Direction.Axis.Y, getSpeed()).setChanged();
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		consumer.accept(shaft);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(shaft);
	}

	@Override
	protected void _delete() {
		shaft.delete();
	}

}

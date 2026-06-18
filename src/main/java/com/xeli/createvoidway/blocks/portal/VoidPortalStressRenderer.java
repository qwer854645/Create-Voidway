package com.xeli.createvoidway.blocks.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class VoidPortalStressRenderer extends KineticBlockEntityRenderer<VoidPortalStressTileEntity> {

	public VoidPortalStressRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(VoidPortalStressTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		if (te.getLevel() == null)
			return;
		if (VisualizationManager.supportsVisualization(te.getLevel()))
			return;

		BlockState state = te.getBlockState();
		if (!(state.getBlock() instanceof IRotate))
			return;

		Direction front = state.getValue(VoidPortalStressBlock.FACING);
		Direction.Axis axis = front.getAxis();
		BlockPos pos = te.getBlockPos();
		float time = AnimationTickHolder.getRenderTime(te.getLevel());

		for (Direction direction : new Direction[] { front, front.getOpposite() }) {
			SuperByteBuffer shaft = VoidPortalStressShaftGeometry.createStub(direction);
			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed() * 3f / 10) % 360;

			if (te.getSpeed() != 0 && te.hasSource()) {
				BlockPos source = te.source.subtract(pos);
				Direction sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
				if (sourceFacing.getAxis() == axis)
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
					angle *= -1;
			}

			angle += offset;
			angle = angle / 180f * (float) Math.PI;
			kineticRotationTransform(shaft, te, axis, angle, light);
			shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}

}

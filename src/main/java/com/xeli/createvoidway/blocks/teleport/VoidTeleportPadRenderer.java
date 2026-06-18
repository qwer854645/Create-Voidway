package com.xeli.createvoidway.blocks.teleport;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import com.xeli.createvoidway.client.ExitHangGuard;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public class VoidTeleportPadRenderer extends KineticBlockEntityRenderer<VoidTeleportPadTileEntity> {

	private static final float BOTTOM_SHAFT_Y_SCALE = 0.15f;
	private static final float BOTTOM_SHAFT_Y_OFFSET = -4.5f / 16f;
	private static final float FRAME_WIDTH = 0.75f;

	public VoidTeleportPadRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(VoidTeleportPadTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		if (ExitHangGuard.shouldSkipRenderLoopWork() || te.getLevel() == null)
			return;
		renderPortalFrame(ms, buffer.getBuffer(RenderType.endPortal()));
		if (te.hasShaftConnection())
			renderBottomShaft(te, ms, buffer, light);
	}

	private void renderPortalFrame(PoseStack ms, VertexConsumer consumer) {
		if (ExitHangGuard.shouldSkipRenderLoopWork())
			return;
		float x = (1F - FRAME_WIDTH) * 0.5F;
		float z = (1F + FRAME_WIDTH) * 0.5F;
		float offset = VoidTeleportPadBlock.PLATE_HEIGHT + 0.001f;
		Matrix4f pose = ms.last().pose();
		consumer.addVertex(pose, x, offset, x);
		consumer.addVertex(pose, z, offset, x);
		consumer.addVertex(pose, z, offset, z);
		consumer.addVertex(pose, x, offset, z);
	}

	private void renderBottomShaft(VoidTeleportPadTileEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		Direction direction = Direction.DOWN;
		SuperByteBuffer shaft = VoidShaftBuffers.partialHalfFacing(direction)
				.translate(0, BOTTOM_SHAFT_Y_OFFSET, 0)
				.center()
				.scale(0, BOTTOM_SHAFT_Y_SCALE, 0)
				.uncenter();

		BlockPos pos = be.getBlockPos();
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float offset = getRotationOffsetForPosition(be, pos, Direction.Axis.Y);
		float angle = (time * be.getSpeed() * 3f / 10) % 360;
		if (be.getSpeed() != 0 && be.hasSource())
			angle += offset;
		angle = angle / 180f * (float) Math.PI;
		kineticRotationTransform(shaft, be, Direction.Axis.Y, angle, light);
		shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}

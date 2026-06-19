package com.xeli.createvoidway.blocks.teleport;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import com.xeli.createvoidway.blocks.voidtypes.VoidPortalOverlay;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import com.xeli.createvoidway.client.ExitHangGuard;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class VoidTeleportPadRenderer extends KineticBlockEntityRenderer<VoidTeleportPadTileEntity>
		implements VoidTileRenderer<VoidTeleportPadTileEntity> {

	private final SkullModelBase skullModelBase;

	public VoidTeleportPadRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	protected void renderSafe(VoidTeleportPadTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		if (ExitHangGuard.shouldSkipRenderLoopWork() || te.getLevel() == null)
			return;
		renderVoid(te, partialTicks, ms, buffer, light, overlay);
		if (te.hasShaftConnection())
			renderBottomShaft(te, ms, buffer, light);
	}

	private void renderBottomShaft(VoidTeleportPadTileEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
		Direction direction = Direction.DOWN;
		SuperByteBuffer shaft = VoidShaftBuffers.bottomCavityStub()
				.translate(0, VoidShaftBuffers.padTerminalShaftDownOffset(), 0);

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

	@Override
	public SkullModelBase getSkullModelBase() {
		return skullModelBase;
	}

	@Override
	public boolean shouldRenderFrame(VoidTeleportPadTileEntity te, Direction direction) {
		return VoidPortalOverlay.isUpFace(direction);
	}

	@Override
	public float getFrameWidth() {
		return VoidPortalOverlay.PAD_FRAME_WIDTH;
	}

	@Override
	public float getFrameOffset(Direction direction) {
		return VoidPortalOverlay.yOffset(direction, VoidTeleportPadBlock.PLATE_HEIGHT + 0.001f);
	}

}

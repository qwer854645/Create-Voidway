package com.xeli.createvoidway.blocks.teleport;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.xeli.createvoidway.blocks.voidtypes.VoidPortalOverlay;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class VoidTeleportLinkRenderer extends SmartBlockEntityRenderer<VoidTeleportLinkTileEntity>
		implements VoidTileRenderer<VoidTeleportLinkTileEntity> {

	private final SkullModelBase skullModelBase;

	public VoidTeleportLinkRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	protected void renderSafe(VoidTeleportLinkTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		renderVoid(te, partialTicks, ms, buffer, light, overlay);
	}

	@Override
	public SkullModelBase getSkullModelBase() {
		return skullModelBase;
	}

	@Override
	public boolean shouldRenderFrame(VoidTeleportLinkTileEntity te, Direction direction) {
		return VoidPortalOverlay.isUpFace(direction);
	}

	@Override
	public float getFrameWidth() {
		return VoidPortalOverlay.LINK_FRAME_WIDTH;
	}

	@Override
	public float getFrameOffset(Direction direction) {
		return VoidPortalOverlay.yOffset(direction, VoidPortalOverlay.LINK_FRAME_Y);
	}

}

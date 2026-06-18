package com.xeli.createvoidway.blocks.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class VoidPortalConnectorRenderer extends SmartBlockEntityRenderer<VoidPortalConnectorTileEntity>
		implements VoidTileRenderer<VoidPortalConnectorTileEntity> {

	private final SkullModelBase skullModelBase;

	public VoidPortalConnectorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	protected void renderSafe(VoidPortalConnectorTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		renderVoid(te, partialTicks, ms, buffer, light, overlay);
	}

	@Override
	public SkullModelBase getSkullModelBase() {
		return skullModelBase;
	}

	@Override
	public boolean shouldRenderFrame(VoidPortalConnectorTileEntity te, Direction direction) {
		return false;
	}

	@Override
	public float getFrameWidth() {
		return 0;
	}

	@Override
	public float getFrameOffset(Direction direction) {
		return 0;
	}

}

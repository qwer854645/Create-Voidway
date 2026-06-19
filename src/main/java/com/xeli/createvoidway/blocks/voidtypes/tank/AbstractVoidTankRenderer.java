package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import com.xeli.createvoidway.blocks.voidtypes.VoidPortalOverlay;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AbstractVoidTankRenderer<T extends AbstractVoidTankTileEntity> extends KineticBlockEntityRenderer<T>
		implements VoidTileRenderer<T> {

	private final SkullModelBase skullModelBase;

	public AbstractVoidTankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	protected void renderSafe(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		renderVoid(te, partialTicks, ms, buffer, light, overlay);

		VoidTank tank = te.getFluidStorage();
		if (!te.isClosed() && !tank.isEmpty()) {
			CatnipServices.FLUID_RENDERER.renderFluidBox(
					tank.getFluid().getFluid().defaultFluidState(),
					.125F, .25F, .125F, .875F, .25F + 0.5F * tank.getFluidAmount() / tank.getCapacity(), .875F,
					buffer, ms, light, false, true);
		}

		if (te.hasShaftConnection())
			renderBottomShaft(te, ms, buffer, light);
	}

	private void renderBottomShaft(T be, PoseStack ms, MultiBufferSource buffer, int light) {
		Direction direction = Direction.DOWN;
		Direction.Axis axis = direction.getAxis();

		SuperByteBuffer shaft = VoidShaftBuffers.bottomCavityStub()
				.translate(0, VoidShaftBuffers.tankShaftDownOffset(), 0);

		BlockPos pos = be.getBlockPos();
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float offset = getRotationOffsetForPosition(be, pos, axis);
		float angle = (time * be.getSpeed() * 3f / 10) % 360;

		if (be.getSpeed() != 0 && be.hasSource()) {
			BlockPos source = be.source.subtract(pos);
			Direction sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
			if (sourceFacing.getAxis() == axis)
				angle *= sourceFacing == direction ? 1 : -1;
			else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
				angle *= -1;
		}

		angle += offset;
		angle = angle / 180f * (float) Math.PI;

		kineticRotationTransform(shaft, be, axis, angle, light);
		shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	public SkullModelBase getSkullModelBase() {
		return skullModelBase;
	}

	@Override
	public boolean shouldRenderFrame(T te, Direction direction) {
		if (te.isClosed())
			return false;
		return VoidPortalOverlay.isUpFace(direction) || direction.getAxis().isHorizontal();
	}

	@Override
	public float getFrameWidth() {
		return VoidPortalOverlay.TANK_WINDOW_FRAME_WIDTH;
	}

	@Override
	public float getFrameOffset(Direction direction) {
		return VoidPortalOverlay.yOffset(direction, 0.251f);
	}

}

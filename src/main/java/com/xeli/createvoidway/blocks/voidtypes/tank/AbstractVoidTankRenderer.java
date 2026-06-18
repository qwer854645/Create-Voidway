package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
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

	/** Bottom frame cavity is 4 voxels tall; keep the shaft stub inside it. */
	private static final float BOTTOM_SHAFT_Y_SCALE = 0.22f;
	private static final float BOTTOM_SHAFT_Y_OFFSET = -4.5f / 16f;

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
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		BlockState state = be.getBlockState();
		Direction direction = Direction.DOWN;
		Direction.Axis axis = direction.getAxis();

		SuperByteBuffer shaft = VoidShaftBuffers.partialHalfFacing(direction)
				.translate(0, BOTTOM_SHAFT_Y_OFFSET, 0)
				.center()
				.scale(0, BOTTOM_SHAFT_Y_SCALE, 0)
				.uncenter();

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
		return !te.isClosed();
	}

	@Override
	public float getFrameWidth() {
		return 0.75f;
	}

	@Override
	public float getFrameOffset(Direction direction) {
		return direction.getAxis() == Direction.Axis.Y ? 0.251f : 0.124f;
	}

}

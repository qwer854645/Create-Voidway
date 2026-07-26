package com.xeli.createvoidway.blocks.voidtypes.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import com.xeli.createvoidway.blocks.voidtypes.VoidPortalOverlay;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.platform.services.ModFluidHelper;
import net.createmod.catnip.render.FluidRenderHelper;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

public class AbstractVoidTankRenderer<T extends AbstractVoidTankTileEntity> extends KineticBlockEntityRenderer<T>
		implements VoidTileRenderer<T> {

	private static final float FLUID_MIN = .125F;
	private static final float FLUID_MAX = .875F;
	private static final float FLUID_Y_BASE = .25F;
	private static final float FLUID_Y_RANGE = .5F;

	private final SkullModelBase skullModelBase;

	public AbstractVoidTankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	protected void renderSafe(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		renderVoid(te, partialTicks, ms, buffer, light, overlay);

		if (!te.isClosed() && !te.getFluidStorage().isEmpty())
			renderFluidLevel(te, ms, buffer, light);

		if (te.hasShaftConnection())
			renderBottomShaft(te, ms, buffer, light);
	}

	/**
	 * Top surface only — side windows must always show end-portal, never fluid sides
	 * (end-portal layers don't occlude translucent fluid reliably).
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void renderFluidLevel(T te, PoseStack ms, MultiBufferSource buffer, int light) {
		VoidTank tank = te.getFluidStorage();
		FluidStack stack = tank.getFluid();
		float yMax = FLUID_Y_BASE + FLUID_Y_RANGE * tank.getFluidAmount() / tank.getCapacity();

		ModFluidHelper helper = CatnipServices.FLUID_HELPER;
		TextureAtlasSprite sprite = helper.getStillTextureOrMissing(stack);
		int color = helper.getColor(stack, null, null);
		int blockLight = Math.max((light >> 4) & 15, helper.getLuminosity(stack));
		int packedLight = (light & 0xF00000) | (blockLight << 4);

		VertexConsumer consumer = FluidRenderHelper.getFluidBuilder(buffer);
		FluidRenderHelper.renderStillTiledFace(
				Direction.UP, FLUID_MIN, FLUID_MIN, FLUID_MAX, FLUID_MAX, yMax,
				consumer, ms, packedLight, color, sprite);
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
		// Four side windows always get end-portal; top opening too (Create Utilities parity).
		return direction.getAxis().isHorizontal() || VoidPortalOverlay.isUpFace(direction);
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

package com.xeli.createvoidway.blocks.voidtypes.battery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.xeli.createvoidway.blocks.VoidShaftBuffers;
import com.xeli.createvoidway.blocks.RWPartialsModels;
import com.xeli.createvoidway.blocks.voidtypes.VoidTileRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
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
import org.joml.Vector3f;

public class AbstractVoidBatteryRenderer<T extends AbstractVoidBatteryTileEntity> extends KineticBlockEntityRenderer<T>
		implements VoidTileRenderer<T> {

	private final SkullModelBase skullModelBase;

	public AbstractVoidBatteryRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	protected void renderSafe(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		renderVoid(te, partialTicks, ms, buffer, light, overlay);
		renderDial(te, partialTicks, ms, buffer, light, overlay);

		if (te.hasShaftConnection())
			renderBottomShaft(te, ms, buffer, light);
	}

	protected void renderDial(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		BlockState state = te.getBlockState();
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		VoidBattery battery = te.getBattery();
		float progress = battery.getMaxEnergyStored() == 0 ? 0
				: (float) battery.getEnergyStored() / battery.getMaxEnergyStored();

		Direction direction = state.getValue(AbstractVoidBatteryBlock.FACING);
		Vector3f vec = new Vector3f(.5f, .375f, .5f)
				.add(direction.step().mul(.625f));

		ms.pushPose();
		CachedBuffers.partial(RWPartialsModels.VOID_BATTERY_DIAL, state)
				.translate(vec)
				.rotateY(180 - direction.toYRot())
				.rotateZ(180 * progress)
				.light(light)
				.renderInto(ms, vb);
		ms.popPose();
	}

	private void renderBottomShaft(T be, PoseStack ms, MultiBufferSource buffer, int light) {
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		Direction direction = Direction.DOWN;
		Direction.Axis axis = direction.getAxis();

		SuperByteBuffer shaft = VoidShaftBuffers.bottomHalfAtFace(1);

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
		return false;
	}

	@Override
	public float getFrameWidth() {
		return .0F;
	}

	@Override
	public float getFrameOffset(Direction direction) {
		return .0F;
	}

}

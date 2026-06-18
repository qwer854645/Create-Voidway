package com.xeli.createvoidway.fluids;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xeli.createvoidway.VoidwayMod;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class VoidTransferFluidClientExtensions implements IClientFluidTypeExtensions {

	public static final VoidTransferFluidClientExtensions INSTANCE = new VoidTransferFluidClientExtensions();

	private static final ResourceLocation STILL = VoidwayMod.asResource("fluid/void_transfer_fluid_still");
	private static final ResourceLocation FLOWING = VoidwayMod.asResource("fluid/void_transfer_fluid_flow");
	private static final Vector3f FOG_COLOR = new Color(0x9D52FF, false).asVectorF();
	private static final float FOG_DISTANCE_MODIFIER = 1f / 8f;

	private VoidTransferFluidClientExtensions() {}

	@Override
	public ResourceLocation getStillTexture() {
		return STILL;
	}

	@Override
	public ResourceLocation getFlowingTexture() {
		return FLOWING;
	}

	@Override
	public int getTintColor(FluidStack stack) {
		return 0xffffffff;
	}

	@Override
	public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		return 0x00ffffff;
	}

	@Override
	public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
			float darkenWorldAmount, Vector3f fluidFogColor) {
		return FOG_COLOR;
	}

	@Override
	public void modifyFogRender(Camera camera, FogMode mode, float renderDistance, float partialTick, float nearDistance,
			float farDistance, FogShape shape) {
		RenderSystem.setShaderFogShape(FogShape.CYLINDER);
		RenderSystem.setShaderFogStart(-8);
		RenderSystem.setShaderFogEnd(96.0f * FOG_DISTANCE_MODIFIER);
	}

}

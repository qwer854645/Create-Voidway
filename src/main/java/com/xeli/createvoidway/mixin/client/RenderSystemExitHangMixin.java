package com.xeli.createvoidway.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xeli.createvoidway.client.ExitHangGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemExitHangMixin {

	@Inject(method = "limitDisplayFPS", at = @At("HEAD"), cancellable = true)
	private static void createvoidway$skipFpsWaitWhileShuttingDown(CallbackInfo ci) {
		if (ExitHangGuard.shouldSkipRenderLoopWork())
			ci.cancel();
	}

	@Inject(method = "flipFrame", at = @At("HEAD"), cancellable = true)
	private static void createvoidway$skipFlipWhileShuttingDown(CallbackInfo ci) {
		if (ExitHangGuard.shouldSkipRenderLoopWork())
			ci.cancel();
	}

}

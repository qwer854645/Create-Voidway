package com.xeli.createvoidway.mixin.client;

import com.xeli.createvoidway.client.ExitHangGuard;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents {@code glfwSwapBuffers} from running after the game loop stops.
 * On some Windows GPU drivers this call hangs indefinitely during {@link Minecraft#destroy()},
 * leaving a black window that must be killed from Task Manager.
 */
@Mixin(Window.class)
public class WindowExitHangMixin {

	@Inject(method = "updateDisplay", at = @At("HEAD"), cancellable = true)
	private void createvoidway$skipSwapWhenNotRunning(CallbackInfo ci) {
		if (ExitHangGuard.shouldSkipRenderLoopWork())
			ci.cancel();
	}

}

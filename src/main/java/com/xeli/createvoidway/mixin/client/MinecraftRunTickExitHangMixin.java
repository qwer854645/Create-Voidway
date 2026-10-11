package com.xeli.createvoidway.mixin.client;

import com.xeli.createvoidway.client.ExitHangGuard;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftRunTickExitHangMixin {

	@Inject(method = "runTick", at = @At("HEAD"), cancellable = true)
	private void createvoidway$skipRunTickWhileShuttingDown(boolean tickWorld, CallbackInfo ci) {
		if (ExitHangGuard.isShuttingDown()) {
			// The vanilla disconnect loop calls runTick while waiting for server saves.
			java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L);
			ci.cancel();
		}
	}

}

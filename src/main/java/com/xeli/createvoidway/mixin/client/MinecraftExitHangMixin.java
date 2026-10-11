package com.xeli.createvoidway.mixin.client;

import com.xeli.createvoidway.client.ExitHangGuard;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skip shutdown rendering without bypassing the integrated server's save/stop wait. */
@Mixin(Minecraft.class)
public abstract class MinecraftExitHangMixin {
    @Inject(method = "stop", at = @At("HEAD"))
    private void createvoidway$markStop(CallbackInfo ci) {
        ExitHangGuard.markShuttingDown();
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void createvoidway$markDestroy(CallbackInfo ci) {
        ExitHangGuard.markShuttingDown();
    }
}

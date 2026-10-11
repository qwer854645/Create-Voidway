package com.xeli.createvoidway.mixin.client;

import com.xeli.createvoidway.client.ExitHangGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skip shutdown rendering without bypassing the integrated server's save/stop wait. */
@Mixin(Minecraft.class)
public abstract class MinecraftExitHangMixin {
    @Shadow @Nullable private IntegratedServer singleplayerServer;
    @Unique private boolean createvoidway$shutdownRequested;

    @Unique
    private void createvoidway$requestShutdown() {
        ExitHangGuard.markShuttingDown();
        if (createvoidway$shutdownRequested || singleplayerServer == null)
            return;
        createvoidway$shutdownRequested = true;
        IntegratedServer server = singleplayerServer;
        // IntegratedServer.halt schedules work on its thread: keep it off the render thread.
        // The original disconnect wait stays intact; this worker also must finish before JVM exit.
        Thread worker = new Thread(() -> server.halt(false), "createvoidway-server-stop");
        worker.setDaemon(false);
        worker.start();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void createvoidway$markStop(CallbackInfo ci) {
        createvoidway$requestShutdown();
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void createvoidway$markDestroy(CallbackInfo ci) {
        createvoidway$requestShutdown();
    }
}

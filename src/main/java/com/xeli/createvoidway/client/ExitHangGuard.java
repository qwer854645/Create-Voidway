package com.xeli.createvoidway.client;

/**
 * Shared shutdown flag for client exit-hang workarounds.
 * {@link net.minecraft.client.Minecraft#isRunning()} is already false during
 * {@link net.minecraft.client.Minecraft#destroy()}, but the destroy path still
 * invokes {@code runTick()} which can block in GLFW event waits.
 */
public final class ExitHangGuard {

	private static volatile boolean shuttingDown;

	private ExitHangGuard() {}

	public static void markShuttingDown() {
		shuttingDown = true;
	}

	public static boolean isShuttingDown() {
		return shuttingDown;
	}

	public static boolean shouldSkipRenderLoopWork() {
		if (shuttingDown)
			return true;
		var mc = net.minecraft.client.Minecraft.getInstance();
		return mc != null && !mc.isRunning();
	}

}

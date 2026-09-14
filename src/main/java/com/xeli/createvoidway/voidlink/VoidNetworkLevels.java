package com.xeli.createvoidway.voidlink;

import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves loaded block entities across dimensions for void frequency networks.
 */
public final class VoidNetworkLevels {

	public record DimPos(ResourceLocation dimension, BlockPos pos) {}

	private VoidNetworkLevels() {}

	@Nullable
	public static MinecraftServer serverOf(LevelAccessor world) {
		if (world instanceof ServerLevel serverLevel)
			return serverLevel.getServer();
		if (world instanceof Level level)
			return level.getServer();
		return null;
	}

	@Nullable
	public static Level resolve(LevelAccessor context, ResourceLocation dimension) {
		if (context instanceof Level level && WorldHelper.getDimensionID(level).equals(dimension))
			return level;
		MinecraftServer server = serverOf(context);
		if (server == null)
			return null;
		return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
	}

	@Nullable
	public static ServerLevel resolveServer(LevelAccessor context, ResourceLocation dimension) {
		Level level = resolve(context, dimension);
		return level instanceof ServerLevel serverLevel ? serverLevel : null;
	}

	/**
	 * True when the chunk is loaded and the block entity is present and not removed.
	 * Unloaded chunks are not considered dead — keep them indexed for cross-chunk/dim links.
	 */
	public static boolean isLoadedAlive(LevelAccessor world, BlockPos pos) {
		if (!world.hasChunkAt(pos))
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity != null && !blockEntity.isRemoved();
	}

	/**
	 * Only remove from the index when the chunk is loaded but the machine is gone.
	 */
	public static boolean shouldDropFromIndex(LevelAccessor world, BlockPos pos) {
		if (!world.hasChunkAt(pos))
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity == null || blockEntity.isRemoved();
	}

}

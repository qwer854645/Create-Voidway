package com.xeli.createvoidway.blocks.portal;

import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportHelper;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class VoidPortalHelper {

	public static final int TELEPORT_DELAY_TICKS = 60;

	public static final String PORTAL_CHARGE_KEY = "CreateVoidwayPortalChargeTicks";

	private VoidPortalHelper() {}

	public static boolean hasContactCooldown(Entity entity) {
		return VoidTeleportHelper.hasContactCooldown(entity);
	}

	public static void clearContactCooldown(Entity entity) {
		VoidTeleportHelper.clearContactCooldown(entity);
	}

	public static boolean canBeTeleported(Entity entity) {
		return VoidTeleportHelper.canBeTeleported(entity);
	}

	public static int getFluidCostFor(Entity entity) {
		return VoidTeleportHelper.getFluidCostFor(entity);
	}

	public static int getPortalChargeTicks(Entity entity) {
		return entity.getPersistentData().getInt(PORTAL_CHARGE_KEY);
	}

	public static void setPortalChargeTicks(Entity entity, int ticks) {
		if (ticks <= 0)
			entity.getPersistentData().remove(PORTAL_CHARGE_KEY);
		else
			entity.getPersistentData().putInt(PORTAL_CHARGE_KEY, ticks);
	}

	public static void clearPortalCharge(Entity entity) {
		setPortalChargeTicks(entity, 0);
	}

	public static boolean isEntityTouchingPortalBlock(Level level, VoidPortalShape shape, Entity entity) {
		AABB entityBox = entity.getBoundingBox();
		for (BlockPos pos : shape.iterateInteriorPositions()) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(RWBlocks.VOID_PORTAL.get()))
				continue;
			VoxelShape voxel = state.getShape(level, pos);
			if (voxel.isEmpty())
				continue;
			AABB blockBox = voxel.bounds().move(pos.getX(), pos.getY(), pos.getZ());
			if (entityBox.intersects(blockBox))
				return true;
		}
		return false;
	}

	public static void teleportTo(ServerLevel destinationLevel, Entity entity, VoidPortalShape destination) {
		destinationLevel.getChunkAt(destination.getSpawnPos());
		BlockPos spawn = destination.getSpawnPos();
		Vec3 target = VoidwaySableCompat.globalTeleportPos(destinationLevel, spawn, 0.05);
		VoidTeleportHelper.teleportEntity(destinationLevel, entity, target);
		entity.fallDistance = 0;
		VoidwaySableCompat.inheritSubLevelVelocity(destinationLevel, entity, target);
		VoidTeleportHelper.setContactCooldown(entity);
		destinationLevel.playSound(null, spawn, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.6f, 1.2f);
		destinationLevel.playSound(null, spawn, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1f);
	}

}

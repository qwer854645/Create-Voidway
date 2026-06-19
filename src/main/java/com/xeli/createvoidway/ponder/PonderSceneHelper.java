package com.xeli.createvoidway.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.xeli.createvoidway.VoidwayMod;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class PonderSceneHelper {

	private PonderSceneHelper() {}

	static void start(CreateSceneBuilder scene, SceneBuildingUtil util, int depth) {
		scene.configureBasePlate(0, 0, depth);
		scene.showBasePlate();
		scene.world().showSection(util.select().layer(0), Direction.UP);
	}

	static void place(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos, BlockState state) {
		scene.world().setBlocks(util.select().position(pos), state, false);
	}

	static String textKey(String sceneId, int index) {
		return VoidwayMod.ID + ".ponder." + sceneId + ".text_" + index;
	}

	static void showText(CreateSceneBuilder scene, String sceneId, int index, Vec3 point, int ticks) {
		scene.overlay().showText(ticks)
				.text(textKey(sceneId, index))
				.pointAt(point);
	}

	static void showTextNear(CreateSceneBuilder scene, String sceneId, int index, Vec3 point, int ticks) {
		scene.overlay().showText(ticks)
				.text(textKey(sceneId, index))
				.placeNearTarget()
				.pointAt(point);
	}

	static Vec3 firstFrequency(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(.15625f, .15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, .15625f + yOffset, .15625f);
			case SOUTH -> faceVec.add(-.15625f, .15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, .15625f + yOffset, -.15625f);
			case UP -> faceVec.add(.15625f + yOffset, shift, -.15625f);
			case DOWN -> faceVec.add(.15625f + yOffset, -shift, .15625f);
		};
	}

	static Vec3 lastFrequency(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(-.15625f, .15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, .15625f + yOffset, -.15625f);
			case SOUTH -> faceVec.add(.15625f, .15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, .15625f + yOffset, .15625f);
			case UP -> faceVec.add(.15625f + yOffset, shift, .15625f);
			case DOWN -> faceVec.add(.15625f + yOffset, -shift, -.15625f);
		};
	}

	static Vec3 ownerSlot(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(0, -.15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, -.15625f + yOffset, 0);
			case SOUTH -> faceVec.add(0, -.15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, -.15625f + yOffset, 0);
			case UP -> faceVec.add(-.15625f + yOffset, shift, 0);
			case DOWN -> faceVec.add(-.15625f + yOffset, -shift, 0);
		};
	}

	/** South-baked horizontal slot overlay (portal connector, teleport link, terminal top). */
	static float southHorizontalShift() {
		return -.0475f;
	}

	static float southHorizontalYOffset() {
		return -.1875f;
	}

	/** North-baked horizontal slot overlay (void motor back face when shaft faces up). */
	static float northHorizontalShift() {
		return .015f;
	}

	static void showFrequencySlots(CreateSceneBuilder scene, Vec3 backFreq, Vec3 frontFreq, Direction face, int ticks) {
		scene.overlay().showFilterSlotInput(backFreq, face, ticks);
		scene.overlay().showFilterSlotInput(frontFreq, face, ticks);
	}

	static void showFilterInput(CreateSceneBuilder scene, Vec3 slot, Direction face, int ticks) {
		scene.overlay().showFilterSlotInput(slot, face, ticks);
	}

	static void showRightClick(CreateSceneBuilder scene, Vec3 pos, Pointing pointing, int ticks) {
		scene.overlay().showControls(pos, pointing, ticks).rightClick();
	}

}

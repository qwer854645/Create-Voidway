package com.xeli.createvoidway.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

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

}

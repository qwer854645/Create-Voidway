package com.xeli.createvoidway.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class VoidIoScenes {

	public static void voidMotorIo(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_motor_io", "Void Motor Inputs and Outputs");
		PonderSceneHelper.start(scene, util, 5);

		BlockPos inputPos = util.grid().at(1, 1, 2);
		BlockPos outputPos = util.grid().at(4, 1, 2);

		scene.world().showSection(util.select().position(inputPos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(outputPos), Direction.DOWN);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_motor_io", 1,
				util.vector().blockSurface(inputPos, Direction.WEST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_motor_io", 2,
				util.vector().blockSurface(outputPos, Direction.EAST), 60);
		scene.idle(70);

		scene.world().setKineticSpeed(util.select().position(inputPos), 32);
		scene.world().setKineticSpeed(util.select().position(outputPos), 32);
		PonderSceneHelper.showText(scene, "void_motor_io", 3,
				util.vector().centerOf(outputPos), 50);
		scene.idle(60);
	}

	public static void voidChestIo(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_chest_io", "Void Chest Inputs and Outputs");
		PonderSceneHelper.start(scene, util, 5);

		BlockPos inputPos = util.grid().at(1, 1, 2);
		BlockPos outputPos = util.grid().at(4, 1, 2);

		scene.world().showSection(util.select().position(inputPos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(outputPos), Direction.DOWN);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_chest_io", 1,
				util.vector().blockSurface(inputPos, Direction.WEST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_chest_io", 2,
				util.vector().blockSurface(outputPos, Direction.EAST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_chest_io", 3,
				util.vector().centerOf(inputPos), 50);
		scene.idle(60);
	}

	public static void voidTankIo(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_tank_io", "Void Tank Inputs and Outputs");
		PonderSceneHelper.start(scene, util, 5);

		BlockPos inputPos = util.grid().at(1, 1, 2);
		BlockPos outputPos = util.grid().at(4, 1, 2);

		scene.world().showSection(util.select().position(inputPos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(outputPos), Direction.DOWN);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_tank_io", 1,
				util.vector().blockSurface(inputPos, Direction.WEST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_tank_io", 2,
				util.vector().blockSurface(outputPos, Direction.EAST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_tank_io", 3,
				util.vector().centerOf(outputPos), 50);
		scene.idle(60);
	}

	public static void voidBatteryIo(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_battery_io", "Void Battery Inputs and Outputs");
		PonderSceneHelper.start(scene, util, 5);

		BlockPos inputPos = util.grid().at(1, 1, 2);
		BlockPos outputPos = util.grid().at(4, 1, 2);

		scene.world().showSection(util.select().position(inputPos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(outputPos), Direction.DOWN);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_battery_io", 1,
				util.vector().blockSurface(inputPos, Direction.WEST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_battery_io", 2,
				util.vector().blockSurface(outputPos, Direction.EAST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_battery_io", 3,
				util.vector().centerOf(inputPos), 50);
		scene.idle(60);
	}

}

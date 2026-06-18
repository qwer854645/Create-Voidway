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

		scene.overlay().showText(60)
				.text("Void Motor Inputs inject stress from a rotating shaft into a shared void channel")
				.pointAt(util.vector().blockSurface(inputPos, Direction.WEST));
		scene.idle(70);

		scene.overlay().showText(60)
				.text("Void Motor Outputs act as adjustable stress sources for the channel they share a Frequency with")
				.pointAt(util.vector().blockSurface(outputPos, Direction.EAST));
		scene.idle(70);

		scene.world().setKineticSpeed(util.select().position(inputPos), 32);
		scene.world().setKineticSpeed(util.select().position(outputPos), 32);
		scene.overlay().showText(50)
				.text("Matching Frequency and Owner pairs an Input with its Outputs across any distance")
				.pointAt(util.vector().centerOf(outputPos));
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

		scene.overlay().showText(60)
				.text("Void Chest Inputs push items into a shared void channel when supplied with stress")
				.pointAt(util.vector().blockSurface(inputPos, Direction.WEST));
		scene.idle(70);

		scene.overlay().showText(60)
				.text("Void Chest Outputs pull items from that channel onto adjacent logistics")
				.pointAt(util.vector().blockSurface(outputPos, Direction.EAST));
		scene.idle(70);

		scene.overlay().showText(50)
				.text("Both sides need a rotating shaft below and Void Transfer Fluid piped into their sides")
				.pointAt(util.vector().centerOf(inputPos));
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

		scene.overlay().showText(60)
				.text("Void Tank Inputs store fluids from adjacent pipes into a shared void channel")
				.pointAt(util.vector().blockSurface(inputPos, Direction.WEST));
		scene.idle(70);

		scene.overlay().showText(60)
				.text("Void Tank Outputs release stored fluids from the channel to nearby pipes")
				.pointAt(util.vector().blockSurface(outputPos, Direction.EAST));
		scene.idle(70);

		scene.overlay().showText(50)
				.text("A rotating shaft below each block is required to drive the transfer")
				.pointAt(util.vector().centerOf(outputPos));
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

		scene.overlay().showText(60)
				.text("Void Battery Inputs deposit FE from adjacent sources into a shared void channel")
				.pointAt(util.vector().blockSurface(inputPos, Direction.WEST));
		scene.idle(70);

		scene.overlay().showText(60)
				.text("Void Battery Outputs supply FE from the channel to adjacent consumers")
				.pointAt(util.vector().blockSurface(outputPos, Direction.EAST));
		scene.idle(70);

		scene.overlay().showText(50)
				.text("Like other void devices, both sides require stress and Void Transfer Fluid to operate")
				.pointAt(util.vector().centerOf(inputPos));
		scene.idle(60);
	}

}

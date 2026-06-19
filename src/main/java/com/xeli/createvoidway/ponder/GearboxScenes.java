package com.xeli.createvoidway.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.blocks.lgearbox.LShapedGearboxBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class GearboxScenes {

	public static void lShapedGearbox(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("lshaped_gearbox", "Relaying Rotational Force using L-Shaped Gearboxes");
		scene.configureBasePlate(0, 0, 5);

		Selection belt = util.select().fromTo(2, 0, 5, 2, 2, 5)
				.add(util.select().position(2, 2, 4));

		scene.showBasePlate();
		scene.world().showSection(belt, Direction.UP);
		scene.idle(10);

		BlockPos cog1Pos = util.grid().at(2, 2, 3);
		BlockPos cog2Pos = util.grid().at(1, 2, 2);

		Selection cog1 = util.select().position(cog1Pos);
		Selection cog2 = util.select().position(cog2Pos);
		BlockPos shaftPos = util.grid().at(0, 2, 2);
		Selection shaft = util.select().position(shaftPos);

		scene.world().showSection(cog1, Direction.SOUTH);
		scene.idle(5);
		scene.world().showSection(cog2.add(shaft), Direction.EAST);
		scene.idle(10);

		scene.overlay().showText(50)
				.pointAt(util.vector().blockSurface(shaftPos, Direction.WEST))
				.placeNearTarget()
				.text(PonderSceneHelper.textKey("lshaped_gearbox", 1));

		scene.idle(50);
		scene.world().hideSection(cog1.add(cog2), Direction.UP);
		scene.idle(20);

		BlockPos gearboxPos = util.grid().at(2, 2, 2);
		Selection gearbox = util.select().position(gearboxPos);
		scene.world().setKineticSpeed(gearbox, 16);

		BlockState shaftState = AllBlocks.SHAFT.getDefaultState();
		scene.world().setBlock(cog1Pos, shaftState.setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
		scene.world().setBlock(cog2Pos, shaftState.setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
		scene.world().showSection(util.select().fromTo(cog1Pos, cog2Pos), Direction.DOWN);
		scene.idle(10);

		scene.overlay().showText(80)
				.colored(PonderPalette.GREEN)
				.pointAt(util.vector().blockSurface(gearboxPos, Direction.NORTH))
				.placeNearTarget()
				.text(PonderSceneHelper.textKey("lshaped_gearbox", 2));
		scene.idle(80);

		scene.world().hideSection(gearbox, Direction.UP);
		scene.idle(20);

		BlockState gearboxState = RWBlocks.LSHAPED_GEARBOX.getDefaultState()
				.setValue(LShapedGearboxBlock.FACING_1, Direction.WEST)
				.setValue(LShapedGearboxBlock.FACING_2, Direction.WEST);
		scene.world().setBlock(gearboxPos, gearboxState, false);
		scene.world().setKineticSpeed(gearbox, -16);
		ElementLink<WorldSectionElement> lGearbox = scene.world().showIndependentSection(gearbox, Direction.DOWN);
		scene.idle(10);

		scene.overlay().showText(80)
				.colored(PonderPalette.GREEN)
				.pointAt(util.vector().blockSurface(gearboxPos, Direction.NORTH))
				.placeNearTarget()
				.text(PonderSceneHelper.textKey("lshaped_gearbox", 3));
		scene.idle(80);
		scene.addKeyframe();

		belt = util.select().fromTo(2, 0, 5, 2, 2, 5)
				.add(util.select().position(2, 2, 4));
		scene.world().hideSection(belt.add(cog1), Direction.SOUTH);
		scene.world().hideSection(cog2.add(shaft), Direction.WEST);
		scene.idle(20);

		scene.world().setKineticSpeed(gearbox, 0);
		scene.rotateCameraY(-90);
		scene.world().moveSection(lGearbox, new Vec3(0, -1, 0), 15);
		scene.idle(30);

		BlockPos lGearboxPos = util.grid().at(2, 1, 2);
		Vec3 lGearboxVec = util.vector().blockSurface(lGearboxPos, Direction.DOWN);
		scene.overlay().showControls(lGearboxVec, Pointing.UP, 40).rightClick().withItem(AllItems.WRENCH.asStack());

		for (int i = 0; i < 8; i++) {
			scene.idle(10);
			scene.world().modifyBlock(gearboxPos, s -> s.cycle(LShapedGearboxBlock.FACING_2), false);
			if (i == 1) {
				scene.overlay().showText(50)
						.text(PonderSceneHelper.textKey("lshaped_gearbox", 4))
						.pointAt(lGearboxVec);
			}
		}
		scene.idle(20);

	}

}

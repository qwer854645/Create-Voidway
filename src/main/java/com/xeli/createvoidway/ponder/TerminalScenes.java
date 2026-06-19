package com.xeli.createvoidway.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalTileEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class TerminalScenes {

	public static void voidNodeTerminal(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_node_terminal", "Using the Void Node Terminal");
		PonderSceneHelper.start(scene, util, 5);

		BlockPos terminalPos = util.grid().at(2, 1, 2);
		BlockPos terminalTopPos = util.grid().at(2, 2, 2);
		BlockPos terminalShaft = util.grid().at(2, 0, 2);
		BlockPos partnerPos = util.grid().at(4, 1, 2);
		BlockPos partnerTopPos = util.grid().at(4, 2, 2);
		BlockPos partnerShaft = util.grid().at(4, 0, 2);

		Selection terminalShaftSel = util.select().position(terminalShaft);
		Selection terminalSel = util.select().position(terminalPos);
		Selection terminalTopSel = util.select().position(terminalTopPos);
		Selection partnerShaftSel = util.select().position(partnerShaft);
		Selection partnerSel = util.select().position(partnerPos);
		Selection partnerTopSel = util.select().position(partnerTopPos);

		scene.world().showSection(terminalShaftSel, Direction.UP);
		scene.idle(5);
		scene.world().showSection(terminalSel, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(terminalTopSel, Direction.DOWN);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_node_terminal", 1,
				util.vector().blockSurface(terminalTopPos, Direction.NORTH), 70);
		scene.idle(80);

		configureFrequency(scene, util, terminalSel, terminalTopPos, Direction.NORTH);

		PonderSceneHelper.showText(scene, "void_node_terminal", 3,
				util.vector().blockSurface(terminalShaft, Direction.UP), 70);
		scene.idle(80);

		scene.world().setKineticSpeed(terminalShaftSel, 32);
		scene.world().showSection(partnerShaftSel, Direction.UP);
		scene.idle(5);
		scene.world().showSection(partnerSel, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(partnerTopSel, Direction.DOWN);
		scene.world().setKineticSpeed(partnerShaftSel, 32);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_node_terminal", 4,
				util.vector().blockSurface(partnerTopPos, Direction.NORTH), 60);
		scene.idle(70);

		Vec3 useVec = util.vector().blockSurface(terminalPos, Direction.UP);
		scene.overlay().showControls(useVec, Pointing.DOWN, 50).rightClick();
		scene.idle(10);
		PonderSceneHelper.showText(scene, "void_node_terminal", 5, useVec, 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_node_terminal", 6,
				util.vector().centerOf(terminalPos), 60);
		scene.idle(70);
	}

	private static void configureFrequency(CreateSceneBuilder scene, SceneBuildingUtil util, Selection terminal,
			BlockPos slotBlockPos, Direction face) {
		Vec3 faceVec = util.vector().blockSurface(slotBlockPos, face);
		float shift = PonderSceneHelper.southHorizontalShift();
		float yOffset = PonderSceneHelper.southHorizontalYOffset();

		Vec3 backFreq = PonderSceneHelper.firstFrequency(faceVec, face, shift, yOffset);
		Vec3 frontFreq = PonderSceneHelper.lastFrequency(faceVec, face, shift, yOffset);

		PonderSceneHelper.showFrequencySlots(scene, backFreq, frontFreq, face, 80);
		scene.idle(10);

		PonderSceneHelper.showTextNear(scene, "void_node_terminal", 2, frontFreq, 50);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		scene.overlay().showControls(frontFreq, Pointing.LEFT, 30).withItem(iron);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(terminal, VoidNodeTerminalTileEntity.class,
				nbt -> nbt.put("FrequencyLast", iron.save(Minecraft.getInstance().level.registryAccess())));

		scene.overlay().showControls(backFreq, Pointing.RIGHT, 30).withItem(sapling);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(terminal, VoidNodeTerminalTileEntity.class,
				nbt -> nbt.put("FrequencyFirst", sapling.save(Minecraft.getInstance().level.registryAccess())));
		scene.idle(20);
	}

}

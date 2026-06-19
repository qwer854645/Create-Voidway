package com.xeli.createvoidway.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkTileEntity;
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

public class TeleportScenes {

	public static void voidTeleport(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_teleport", "Using Void Teleport Pads");
		PonderSceneHelper.start(scene, util, 7);

		BlockPos padPos = util.grid().at(2, 1, 3);
		BlockPos linkPos = util.grid().at(3, 1, 3);
		BlockPos shaftPos = util.grid().at(2, 0, 3);
		BlockPos partnerPadPos = util.grid().at(5, 1, 3);

		Selection pad = util.select().position(padPos);
		Selection link = util.select().position(linkPos);
		Selection shaft = util.select().position(shaftPos);
		Selection partner = util.select().position(partnerPadPos);

		scene.world().showSection(shaft, Direction.UP);
		scene.idle(5);
		scene.world().showSection(pad, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(link, Direction.WEST);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_teleport", 1,
				util.vector().blockSurface(padPos, Direction.NORTH), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_teleport", 2,
				util.vector().blockSurface(linkPos, Direction.NORTH), 60);
		scene.idle(70);

		configureFrequency(scene, util, link, linkPos, Direction.NORTH);

		scene.addKeyframe();
		Vec3 bindLinkVec = util.vector().blockSurface(linkPos, Direction.UP);
		scene.overlay().showControls(bindLinkVec, Pointing.DOWN, 50)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
		scene.idle(10);
		PonderSceneHelper.showText(scene, "void_teleport", 4, bindLinkVec, 50);
		scene.idle(60);

		Vec3 bindPadVec = util.vector().blockSurface(padPos, Direction.UP);
		scene.overlay().showControls(bindPadVec, Pointing.DOWN, 50)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
		scene.idle(40);

		scene.world().showSection(partner, Direction.DOWN);
		scene.idle(10);
		PonderSceneHelper.showText(scene, "void_teleport", 5,
				util.vector().blockSurface(partnerPadPos, Direction.NORTH), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_teleport", 6,
				util.vector().blockSurface(shaftPos, Direction.UP), 60);
		scene.idle(70);
	}

	private static void configureFrequency(CreateSceneBuilder scene, SceneBuildingUtil util, Selection link,
			BlockPos linkPos, Direction face) {
		Vec3 linkVec = util.vector().blockSurface(linkPos, face);
		float shift = PonderSceneHelper.southHorizontalShift();
		float yOffset = PonderSceneHelper.southHorizontalYOffset();

		Vec3 backFreq = PonderSceneHelper.firstFrequency(linkVec, face, shift, yOffset);
		Vec3 frontFreq = PonderSceneHelper.lastFrequency(linkVec, face, shift, yOffset);

		PonderSceneHelper.showFrequencySlots(scene, backFreq, frontFreq, face, 80);
		scene.idle(10);

		PonderSceneHelper.showTextNear(scene, "void_teleport", 3, frontFreq, 50);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		scene.overlay().showControls(frontFreq, Pointing.LEFT, 30).withItem(iron);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(link, VoidTeleportLinkTileEntity.class,
				nbt -> nbt.put("FrequencyLast", iron.save(Minecraft.getInstance().level.registryAccess())));

		scene.overlay().showControls(backFreq, Pointing.RIGHT, 30).withItem(sapling);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(link, VoidTeleportLinkTileEntity.class,
				nbt -> nbt.put("FrequencyFirst", sapling.save(Minecraft.getInstance().level.registryAccess())));
		scene.idle(20);
	}

}

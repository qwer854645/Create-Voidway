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

		Vec3 padVec = util.vector().blockSurface(padPos, Direction.NORTH);
		scene.overlay().showText(60)
				.text("Void Teleport Pads instantly move every entity standing on them to a paired pad")
				.pointAt(padVec);
		scene.idle(70);

		Vec3 linkVec = util.vector().blockSurface(linkPos, Direction.NORTH);
		scene.overlay().showText(60)
				.text("Place a Void Teleport Link coplanar and adjacent to configure the pad's Frequency")
				.pointAt(linkVec);
		scene.idle(70);

		configureFrequency(scene, util, link, linkPos, Direction.NORTH);

		scene.addKeyframe();
		Vec3 bindLinkVec = util.vector().blockSurface(linkPos, Direction.UP);
		scene.overlay().showControls(bindLinkVec, Pointing.DOWN, 50)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
		scene.idle(10);
		scene.overlay().showText(50)
				.text("Wrench the Link, then wrench the adjacent Pad to bind them together")
				.pointAt(bindLinkVec);
		scene.idle(60);

		Vec3 bindPadVec = util.vector().blockSurface(padPos, Direction.UP);
		scene.overlay().showControls(bindPadVec, Pointing.DOWN, 50)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
		scene.idle(40);

		scene.world().showSection(partner, Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(60)
				.text("Build a second Link + Pad pair with the same Frequency — only one partner pad is allowed per channel")
				.pointAt(util.vector().blockSurface(partnerPadPos, Direction.NORTH));
		scene.idle(70);

		scene.overlay().showText(60)
				.text("Supply a rotating shaft below each pad and keep Void Transfer Fluid available to enable teleporting")
				.pointAt(util.vector().blockSurface(shaftPos, Direction.UP));
		scene.idle(70);
	}

	private static void configureFrequency(CreateSceneBuilder scene, SceneBuildingUtil util, Selection link,
			BlockPos linkPos, Direction face) {
		Vec3 linkVec = util.vector().blockSurface(linkPos, face);
		float shift = -.0475f;
		float yOffset = -.1875f;

		Vec3 backFreq = offsetFirstFrequency(linkVec, face, shift, yOffset);
		Vec3 frontFreq = offsetLastFrequency(linkVec, face, shift, yOffset);

		scene.overlay().showFilterSlotInput(backFreq, face, 80);
		scene.overlay().showFilterSlotInput(frontFreq, face, 80);
		scene.idle(10);

		scene.overlay().showText(50)
				.text("Place items in the Link's two upper slots to define its Frequency")
				.placeNearTarget()
				.pointAt(frontFreq);
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

	private static Vec3 offsetFirstFrequency(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(.15625f, .15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, .15625f + yOffset, .15625f);
			case SOUTH -> faceVec.add(-.15625f, .15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, .15625f + yOffset, -.15625f);
			default -> faceVec;
		};
	}

	private static Vec3 offsetLastFrequency(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(-.15625f, .15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, .15625f + yOffset, -.15625f);
			case SOUTH -> faceVec.add(.15625f, .15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, .15625f + yOffset, .15625f);
			default -> faceVec;
		};
	}

}

package com.xeli.createvoidway.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.blocks.portal.VoidPortalConnectorTileEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class PortalScenes {

	public static void voidPortal(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_portal", "Using Void Portals");
		PonderSceneHelper.start(scene, util, 7);
		scene.setSceneOffsetY(-0.5f);

		PortalBuild build = layout(util.grid().at(0, 0, 0));

		Selection frame = util.select().fromTo(build.left, build.bottom, build.planeZ, build.right, build.top, build.planeZ);
		Selection shaft = util.select().position(build.shaftPos);

		scene.world().showSection(frame, Direction.SOUTH);
		scene.idle(20);

		scene.overlay().showText(60)
				.text("Build a hollow square frame of Void Portal Frame blocks with a square opening")
				.pointAt(util.vector().blockSurface(build.connectorPos, Direction.SOUTH));
		scene.idle(70);

		scene.overlay().showText(70)
				.text("The bottom row must contain exactly one Fluid Port, Connector, and Stress Port — in any order, side by side")
				.pointAt(util.vector().blockSurface(build.fluidPos, Direction.SOUTH));
		scene.idle(80);

		configureConnectorFrequency(scene, util, build.connectorPos);

		scene.addKeyframe();
		PonderSceneHelper.place(scene, util, build.shaftPos,
				AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.Z));
		scene.world().showSection(shaft, Direction.SOUTH);
		scene.world().setKineticSpeed(shaft, 32);

		scene.overlay().showText(60)
				.text("Connect a rotating shaft to the Stress Port's front or back face")
				.pointAt(util.vector().blockSurface(build.stressPos, Direction.NORTH));
		scene.idle(70);

		scene.overlay().showText(60)
				.text("Pipe Void Transfer Fluid into the Fluid Port to fuel the portal")
				.pointAt(util.vector().blockSurface(build.fluidPos, Direction.SOUTH));
		scene.idle(70);

		fillPortalInterior(scene, util, build);
		scene.idle(10);

		scene.overlay().showText(70)
				.text("When paired with another valid portal on the same Frequency, the opening fills and entities teleport after standing inside for 3 seconds")
				.pointAt(util.vector().centerOf(build.connectorPos.above(2)));
		scene.idle(80);
	}

	private static void configureConnectorFrequency(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos connectorPos) {
		Selection connector = util.select().position(connectorPos);
		Direction face = Direction.SOUTH;
		Vec3 faceVec = util.vector().blockSurface(connectorPos, face);
		float shift = .015f;

		Vec3 backFreq = faceVec.add(.15625f, .15625f, shift);
		Vec3 frontFreq = faceVec.add(-.15625f, .15625f, shift);

		scene.overlay().showFilterSlotInput(backFreq, face, 80);
		scene.overlay().showFilterSlotInput(frontFreq, face, 80);
		scene.idle(10);

		scene.overlay().showText(50)
				.text("Set the Connector's Frequency like other void devices to pair with a remote portal")
				.placeNearTarget()
				.pointAt(frontFreq);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		scene.overlay().showControls(frontFreq, Pointing.LEFT, 30).withItem(iron);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(connector, VoidPortalConnectorTileEntity.class,
				nbt -> nbt.put("FrequencyLast", iron.save(Minecraft.getInstance().level.registryAccess())));

		scene.overlay().showControls(backFreq, Pointing.RIGHT, 30).withItem(sapling);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(connector, VoidPortalConnectorTileEntity.class,
				nbt -> nbt.put("FrequencyFirst", sapling.save(Minecraft.getInstance().level.registryAccess())));
		scene.idle(20);
	}

	private static void fillPortalInterior(CreateSceneBuilder scene, SceneBuildingUtil util, PortalBuild build) {
		BlockState portal = RWBlocks.VOID_PORTAL.getDefaultState()
				.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X);
		for (int x = build.left + 1; x < build.right; x++) {
			for (int y = build.bottom + 1; y < build.top; y++) {
				BlockPos pos = new BlockPos(x, y, build.planeZ);
				PonderSceneHelper.place(scene, util, pos, portal);
				scene.world().showSection(util.select().position(pos), Direction.SOUTH);
			}
		}
	}

	private static PortalBuild layout(BlockPos origin) {
		int left = origin.getX();
		int bottom = origin.getY();
		int planeZ = origin.getZ();
		int right = left + 4;
		int top = bottom + 4;
		return new PortalBuild(left, right, bottom, top, planeZ,
				new BlockPos(left + 1, bottom, planeZ),
				new BlockPos(left + 2, bottom, planeZ),
				new BlockPos(left + 3, bottom, planeZ),
				new BlockPos(left + 3, bottom, planeZ).relative(Direction.NORTH));
	}

	private record PortalBuild(int left, int right, int bottom, int top, int planeZ,
			BlockPos fluidPos, BlockPos connectorPos, BlockPos stressPos, BlockPos shaftPos) {}

}

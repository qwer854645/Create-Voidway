package com.xeli.createvoidway.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.xeli.createvoidway.blocks.voidtypes.battery.AbstractVoidBatteryTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestOutputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorOutputTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.tank.AbstractVoidTankTileEntity;
import com.xeli.createvoidway.fluids.RWFluids;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.function.Consumer;

public class VoidScenes {

	public static void voidMotor(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("void_motor", "Using Void Motors");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.world().showSection(util.select().position(5, 0, 2), Direction.UP);

		Selection source = util.select().fromTo(5, 1, 1, 4, 1, 1);
		Selection receiver = util.select().fromTo(1, 1, 2, 2, 1, 2);

		scene.world().showSection(source, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(receiver, Direction.DOWN);
		scene.idle(10);

		BlockPos sourcePos = util.grid().at(4, 1, 1);
		BlockPos receiverPos = util.grid().at(1, 1, 2);

		playVoidSequence(
				scene, util, "void_motor", VoidMotorOutputTileEntity.class,
				PonderSceneHelper.northHorizontalShift(), 0,
				sourcePos, receiverPos,
				Direction.WEST, Direction.WEST,
				(pos) -> scene.world().setKineticSpeed(receiver, 0),
				(pos) -> scene.world().setKineticSpeed(receiver, -32),
				false, false
		);

	}

	public static void voidChest(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("void_chest", "Using Void Chests");
		scene.configureBasePlate(1, 0, 5);
		scene.showBasePlate();
		scene.world().showSection(util.select().position(0, 0, 3)
				.add(util.select().position(6, 0, 3)), Direction.UP);

		Selection source = util.select().fromTo(4, 1, 0, 6, 2, 3);
		Selection receiver = util.select().fromTo(0, 1, 0, 2, 2, 3);

		scene.idle(10);
		scene.world().showSection(source, Direction.DOWN);
		scene.world().showSection(receiver, Direction.DOWN);
		scene.idle(10);

		ParallelInstruction parallel = new ParallelInstruction(scene);

		BlockPos sourceEntryBelt = util.grid().at(4, 1, 0);
		BlockPos sourceExitBelt = util.grid().at(4, 1, 2);
		BlockPos receiverEntryBelt = util.grid().at(2, 1, 2);
		ItemStack stack = AllBlocks.BRASS_BLOCK.asStack();

		Vec3 motion = new Vec3(0, -.2, 0);
		for (int i = 0; i < 27; i++) {

			ElementLink<EntityElement> item = parallel.scene.world().createItemEntity(
					util.vector().of(4.75, 3, 0.5), motion, stack);
			parallel.scene.idle(5);

			parallel.scene.world().modifyEntity(item, Entity::discard);
			parallel.scene.world().createItemOnBelt(sourceEntryBelt, Direction.EAST, stack);
			parallel.scene.idle(16);

			parallel.scene.world().removeItemsFromBelt(sourceExitBelt);
			parallel.scene.world().flapFunnel(sourceExitBelt.above(), false);

			if (i < 6 || i > 21) {
				parallel.scene.world().createItemOnBelt(receiverEntryBelt, Direction.EAST, stack);
				parallel.scene.world().flapFunnel(receiverEntryBelt.above(), true);
			}

		}

		scene.addInstruction(parallel);

		BlockPos sourcePos = util.grid().at(4, 2, 3);
		BlockPos receiverPos = util.grid().at(2, 2, 3);

		playVoidSequence(
				scene, util, "void_chest", VoidChestOutputTileEntity.class,
				PonderSceneHelper.southHorizontalShift(), PonderSceneHelper.southHorizontalYOffset(),
				sourcePos, receiverPos,
				Direction.SOUTH, Direction.SOUTH,
				(pos) -> {},
				(pos) -> {},
				true, false
		);

	}

	public static void voidTank(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("void_tank", "Using Void Tanks");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection pipes = util.select().fromTo(1, 0, 5, 3, 1, 5)
				.add(util.select().fromTo(1, 1, 4, 3, 1, 4));

		BlockPos sourcePos = util.grid().at(1, 1, 3);
		BlockPos secSourcePos = util.grid().at(3, 1, 3);
		BlockPos receiverPos = util.grid().at(2, 1, 1);

		Selection source = util.select().position(sourcePos);
		Selection secSource = util.select().position(secSourcePos);
		Selection receiver = util.select().position(receiverPos);

		scene.world().modifyBlockEntity(sourcePos, AbstractVoidTankTileEntity.class,
				te -> te.getFluidStorage().setFluid(FluidStack.EMPTY));

		scene.world().modifyBlockEntity(secSourcePos, AbstractVoidTankTileEntity.class,
				te -> te.getFluidStorage().setFluid(FluidStack.EMPTY));

		scene.idle(10);
		scene.world().showSection(pipes, Direction.NORTH);
		scene.world().showSection(source, Direction.SOUTH);
		scene.world().showSection(secSource, Direction.SOUTH);
		scene.idle(10);
		scene.world().showSection(receiver, Direction.DOWN);
		scene.idle(10);

		ParallelInstruction parallel = new ParallelInstruction(scene);

		FluidStack honey = new FluidStack(AllFluids.HONEY.get(), 500);
		FluidStack lava = new FluidStack(Fluids.LAVA, 500);
		for (int i = 0; i < 8; i++) {

			parallel.scene.world().modifyBlockEntity(sourcePos, AbstractVoidTankTileEntity.class,
					te -> te.getFluidStorage().fill(honey, IFluidHandler.FluidAction.EXECUTE));

			parallel.scene.world().modifyBlockEntity(secSourcePos, AbstractVoidTankTileEntity.class,
					te -> te.getFluidStorage().fill(lava, IFluidHandler.FluidAction.EXECUTE));

			parallel.scene.idle(15);
		}

		scene.addInstruction(parallel);

		playVoidSequence(
				scene, util, "void_tank", AbstractVoidTankTileEntity.class,
				PonderSceneHelper.northHorizontalShift(), 0,
				sourcePos, receiverPos,
				Direction.UP, Direction.UP,
				(pos) -> {},
				(pos) -> scene.world().modifyBlockEntity(pos, AbstractVoidTankTileEntity.class,
						te -> {
							lava.setAmount(4000);
							te.getFluidStorage().setFluid(lava);
						}),
				false, true
		);

	}

	public static void voidBattery(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("void_battery", "Using Void Batteries");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.world().showSection(util.select().layer(0), Direction.UP);

		BlockPos sourcePos = util.grid().at(3, 1, 2);
		BlockPos receiverPos = util.grid().at(1, 1, 2);

		scene.world().showSection(util.select().position(sourcePos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(receiverPos), Direction.DOWN);
		scene.idle(10);

		playVoidSequence(
				scene, util, "void_battery", AbstractVoidBatteryTileEntity.class,
				PonderSceneHelper.southHorizontalShift(), PonderSceneHelper.southHorizontalYOffset(),
				sourcePos, receiverPos,
				Direction.SOUTH, Direction.SOUTH,
				(pos) -> {},
				(pos) -> {},
				true, false
		);

	}

	public static void voidTransferFluid(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("void_transfer_fluid", "Void Transfer Fluid");
		PonderSceneHelper.start(scene, util, 5);

		BlockPos chestPos = util.grid().at(2, 1, 2);
		BlockPos tankPos = util.grid().at(3, 1, 2);

		scene.world().showSection(util.select().position(chestPos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(tankPos), Direction.WEST);
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_transfer_fluid", 1,
				util.vector().blockSurface(chestPos, Direction.NORTH), 60);
		scene.idle(70);

		scene.overlay().showControls(util.vector().blockSurface(tankPos, Direction.WEST), Pointing.RIGHT, 40)
				.withItem(RWFluids.getBucketStack());
		scene.idle(10);

		PonderSceneHelper.showText(scene, "void_transfer_fluid", 2,
				util.vector().blockSurface(chestPos, Direction.EAST), 60);
		scene.idle(70);

		PonderSceneHelper.showText(scene, "void_transfer_fluid", 3,
				util.vector().topOf(chestPos), 50);
		scene.idle(60);
	}

	private static void playVoidSequence(CreateSceneBuilder scene,
										 SceneBuildingUtil util,
										 String sceneId,
										 Class<? extends BlockEntity> beType,
										 float shift, float yOffset,
										 BlockPos firstPos,
										 BlockPos secondPos,
										 Direction firstDirection,
										 Direction secondDirection,
										 Consumer<BlockPos> onDisconnect,
										 Consumer<BlockPos> onConnect,
										 boolean rotate,
										 boolean isTank) {

		Selection firstBlock = util.select().position(firstPos);
		Vec3 firstVec = util.vector().blockSurface(firstPos, firstDirection);

		Selection secondBlock = util.select().position(secondPos);
		Vec3 secondVec = util.vector().blockSurface(secondPos, secondDirection);

		PonderSceneHelper.showText(scene, sceneId, 1, firstVec, 50);
		scene.idle(50);

		if (rotate) scene.rotateCameraY(-90);
		scene.addKeyframe();

		Vec3 firstBackFreq = PonderSceneHelper.firstFrequency(firstVec, firstDirection, shift, yOffset);
		Vec3 firstFrontFreq = PonderSceneHelper.lastFrequency(firstVec, firstDirection, shift, yOffset);
		Vec3 firstOwner = PonderSceneHelper.ownerSlot(firstVec, firstDirection, shift, yOffset);

		Vec3 secondBackFreq = PonderSceneHelper.firstFrequency(secondVec, secondDirection, shift, yOffset);
		Vec3 secondFrontFreq = PonderSceneHelper.lastFrequency(secondVec, secondDirection, shift, yOffset);

		scene.idle(10);
		PonderSceneHelper.showFrequencySlots(scene, firstBackFreq, firstFrontFreq, firstDirection, 100);
		scene.idle(10);

		PonderSceneHelper.showTextNear(scene, sceneId, 2, firstFrontFreq, 50);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		showFrequency(scene, firstBlock, beType, firstFrontFreq, "FrequencyLast", Pointing.LEFT, iron);
		onDisconnect.accept(secondPos);
		showFrequency(scene, firstBlock, beType, firstBackFreq, "FrequencyFirst", Pointing.RIGHT, sapling);

		if (isTank) onConnect.accept(firstPos);

		scene.idle(30);

		scene.addKeyframe();
		scene.idle(10);
		PonderSceneHelper.showFilterInput(scene, firstOwner, firstDirection, 100);
		scene.idle(10);

		PonderSceneHelper.showRightClick(scene, firstOwner, Pointing.UP, 40);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(firstBlock, beType, nbt -> nbt.remove("Owner"));

		PonderSceneHelper.showTextNear(scene, sceneId, 3, firstOwner, 50);
		scene.idle(60);

		PonderSceneHelper.showRightClick(scene, firstOwner, Pointing.UP, 40);
		scene.idle(7);
		scene.world().restoreBlocks(firstBlock);
		scene.world().modifyBlockEntityNBT(firstBlock, beType, nbt -> {
			nbt.put("FrequencyFirst", sapling.save(Minecraft.getInstance().level.registryAccess()));
			nbt.put("FrequencyLast", iron.save(Minecraft.getInstance().level.registryAccess()));
		});

		if (isTank) onConnect.accept(firstPos);

		PonderSceneHelper.showTextNear(scene, sceneId, 4, firstOwner, 50);
		scene.idle(60);

		PonderSceneHelper.showTextNear(scene, sceneId, 5, firstVec, 50);
		scene.idle(60);

		scene.addKeyframe();
		scene.idle(10);

		PonderSceneHelper.showTextNear(scene, sceneId, 6, secondVec, 60);
		scene.idle(70);

		showFrequency(scene, secondBlock, beType, secondFrontFreq, "FrequencyLast", Pointing.LEFT, iron);
		showFrequency(scene, secondBlock, beType, secondBackFreq, "FrequencyFirst", Pointing.RIGHT, sapling);
		onConnect.accept(secondPos);

		if (rotate) {
			scene.idle(20);
			scene.rotateCameraY(90);
			scene.idle(30);
		} else scene.idle(50);

	}

	private static void showFrequency(CreateSceneBuilder scene,
									  Selection block,
									  Class<? extends BlockEntity> beType,
									  Vec3 slotPos,
									  String slotId,
									  Pointing pointing,
									  ItemStack item) {
		scene.overlay().showControls(slotPos, pointing, 30).withItem(item);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(block, beType,
				nbt -> nbt.put(slotId, item.save(Minecraft.getInstance().level.registryAccess())));
	}

}

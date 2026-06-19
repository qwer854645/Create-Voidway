package com.xeli.createvoidway.blocks;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.decoration.MetalLadderBlock;
import com.simibubi.create.content.decoration.MetalScaffoldingBlock;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.xeli.createvoidway.blocks.gearcube.GearcubeBlock;
import com.xeli.createvoidway.blocks.lgearbox.LShapedGearboxBlock;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryInputBlock;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryOutputBlock;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestInputBlock;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestOutputBlock;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorInputBlock;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorOutputBlock;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankInputBlock;
import com.xeli.createvoidway.blocks.voidtypes.tank.VoidTankOutputBlock;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkBlock;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportPadBlock;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalBlock;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalTopBlock;
import com.xeli.createvoidway.blocks.portal.VoidPortalBlock;
import com.xeli.createvoidway.blocks.portal.VoidPortalConnectorBlock;
import com.xeli.createvoidway.blocks.portal.VoidPortalFluidBlock;
import com.xeli.createvoidway.blocks.portal.VoidPortalFrameBlock;
import com.xeli.createvoidway.blocks.portal.VoidPortalStressBlock;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.mountedstorage.RWMountedStorages;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.xeli.createvoidway.VoidwayMod.REGISTRATE;

public class RWBlocks {

	public static final BlockEntry<Block> VOID_STEEL_BLOCK = REGISTRATE.block("void_steel_block", Block::new)
			.initialProperties(() -> Blocks.NETHERITE_BLOCK)
			.properties(p -> p.mapColor(MapColor.COLOR_GREEN))
			.properties(p -> p.strength(55.0F, 1200.0F))
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	public static final BlockEntry<MetalScaffoldingBlock> VOID_STEEL_SCAFFOLD = REGISTRATE.block("void_steel_scaffolding", MetalScaffoldingBlock::new)
			.transform(BuilderTransformers.scaffold("void_steel",
					() -> DataIngredient.items(RWItems.VOID_STEEL_INGOT.get()), MapColor.COLOR_GREEN,
					RWSpriteShifts.VOID_STEEL_SCAFFOLD, RWSpriteShifts.VOID_STEEL_SCAFFOLD_INSIDE, RWSpriteShifts.VOID_CASING))
			.register();

	public static final BlockEntry<MetalLadderBlock> VOID_STEEL_LADDER = REGISTRATE.block("void_steel_ladder", MetalLadderBlock::new)
			.transform(BuilderTransformers.ladder("void_steel",
					() -> DataIngredient.items(RWItems.VOID_STEEL_INGOT.get()), MapColor.COLOR_GREEN))
			.register();

	public static final BlockEntry<IronBarsBlock> VOID_STEEL_BARS = REGISTRATE.block("void_steel_bars", IronBarsBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(() -> Blocks.IRON_BARS)
			.properties(p -> p.sound(SoundType.COPPER).mapColor(MapColor.COLOR_GREEN))
			.tag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
			.tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
			.transform(pickaxeOnly())
			.item()
			.build()
			.register();

	public static final BlockEntry<CasingBlock> VOID_CASING = REGISTRATE.block("void_casing", CasingBlock::new)
			.transform(BuilderTransformers.casing(() -> RWSpriteShifts.VOID_CASING))
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(55.0F, 1200.0F))
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.register();

	public static final BlockEntry<VoidMotorOutputBlock> VOID_MOTOR_OUTPUT = REGISTRATE.block("void_motor_output",
			VoidMotorOutputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidMotorInputBlock> VOID_MOTOR_INPUT = REGISTRATE.block("void_motor_input",
			VoidMotorInputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidChestOutputBlock> VOID_CHEST_OUTPUT = REGISTRATE.block("void_chest_output",
			VoidChestOutputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.transform(MountedItemStorageType.mountedItemStorage(RWMountedStorages.VOID_CHEST))
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidChestInputBlock> VOID_CHEST_INPUT = REGISTRATE.block("void_chest_input",
			VoidChestInputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.transform(MountedItemStorageType.mountedItemStorage(RWMountedStorages.VOID_CHEST))
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidTankOutputBlock> VOID_TANK_OUTPUT = REGISTRATE.block("void_tank_output",
			VoidTankOutputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidTankInputBlock> VOID_TANK_INPUT = REGISTRATE.block("void_tank_input",
			VoidTankInputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidBatteryOutputBlock> VOID_BATTERY_OUTPUT = REGISTRATE.block("void_battery_output",
			VoidBatteryOutputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidBatteryInputBlock> VOID_BATTERY_INPUT = REGISTRATE.block("void_battery_input",
			VoidBatteryInputBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidTeleportLinkBlock> VOID_TELEPORT_LINK = REGISTRATE.block("void_teleport_link",
			VoidTeleportLinkBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidTeleportPadBlock> VOID_TELEPORT_PAD = REGISTRATE.block("void_teleport_pad",
			VoidTeleportPadBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidPortalFrameBlock> VOID_PORTAL_FRAME = REGISTRATE.block("void_portal_frame",
			VoidPortalFrameBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(55.0F, 1200.0F))
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	public static final BlockEntry<VoidPortalFluidBlock> VOID_PORTAL_FLUID = REGISTRATE.block("void_portal_fluid",
			VoidPortalFluidBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidPortalStressBlock> VOID_PORTAL_STRESS = REGISTRATE.block("void_portal_stress",
			VoidPortalStressBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidPortalConnectorBlock> VOID_PORTAL_CONNECTOR = REGISTRATE.block("void_portal_connector",
			VoidPortalConnectorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidPortalBlock> VOID_PORTAL = REGISTRATE.block("void_portal", VoidPortalBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.noCollission())
			.properties(p -> p.noLootTable())
			.properties(p -> p.lightLevel(state -> 11))
			.properties(p -> p.strength(-1.0F, 3600000.0F))
			.addLayer(() -> RenderType::translucent)
			.register();

	public static final BlockEntry<VoidNodeTerminalBlock> VOID_NODE_TERMINAL = REGISTRATE.block("void_node_terminal",
			VoidNodeTerminalBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	public static final BlockEntry<VoidNodeTerminalTopBlock> VOID_NODE_TERMINAL_TOP = REGISTRATE
			.block("void_node_terminal_top", VoidNodeTerminalTopBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.properties(p -> p.noLootTable())
			.transform(pickaxeOnly())
			.register();

	public static final BlockEntry<GearcubeBlock> GEARCUBE = REGISTRATE.block("gearcube", GearcubeBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			//.transform(CStress.setNoImpact())
			.transform(axeOrPickaxe())
			.simpleItem()
			.register();

	public static final BlockEntry<LShapedGearboxBlock> LSHAPED_GEARBOX = REGISTRATE.block("lshaped_gearbox", LShapedGearboxBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			//.transform(CStress.setNoImpact())
			.transform(axeOrPickaxe())
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
					(state, face) -> !LShapedGearboxBlock.hasShaftTowards(state, face))))
			.simpleItem()
			.register();

	public static final BlockEntry<Block> AMETHYST_TILES = REGISTRATE.block("amethyst_tiles", Block::new)
			.initialProperties(() -> Blocks.DEEPSLATE)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops())
			.properties(p -> p.sound(SoundType.AMETHYST_CLUSTER))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	public static final BlockEntry<Block> SMALL_AMETHYST_TILES = REGISTRATE.block("small_amethyst_tiles", Block::new)
			.initialProperties(() -> Blocks.DEEPSLATE)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops())
			.properties(p -> p.sound(SoundType.AMETHYST_CLUSTER))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	public static void register() {}

}

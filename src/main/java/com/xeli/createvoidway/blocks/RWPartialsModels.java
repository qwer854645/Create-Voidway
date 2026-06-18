package com.xeli.createvoidway.blocks;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.xeli.createvoidway.VoidwayMod;

public class RWPartialsModels {

	public static final PartialModel VOID_CHEST_LID = block("void_chest/lid");

	public static final PartialModel VOID_BATTERY_DIAL = block("void_battery/dial");

	private static PartialModel block(String path) {
		return PartialModel.of(VoidwayMod.asResource("block/" + path));
	}

	public static void init() {}

}

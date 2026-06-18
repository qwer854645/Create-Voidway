package com.xeli.createvoidway;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class RWTags {

	public static final TagKey<Fluid> VOID_TRANSFER_FLUID = TagKey.create(Registries.FLUID,
			VoidwayMod.asResource("void_transfer_fluid"));

	private RWTags() {}

}

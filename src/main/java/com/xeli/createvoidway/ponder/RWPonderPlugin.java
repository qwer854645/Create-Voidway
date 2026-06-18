package com.xeli.createvoidway.ponder;

import com.xeli.createvoidway.VoidwayMod;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RWPonderPlugin implements PonderPlugin {

    @Override
    public @NotNull String getModId() {
        return VoidwayMod.ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        RWPonders.registerScenes(helper);
    }

}

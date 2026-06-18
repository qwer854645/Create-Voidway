package com.xeli.createvoidway.mountedstorage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

import static com.xeli.createvoidway.VoidwayMod.REGISTRATE;

public class RWMountedStorages {

    public static final RegistryEntry<MountedItemStorageType<?>, VoidChestMountedStorageType> VOID_CHEST =
            simpleItem("void_chest", VoidChestMountedStorageType::new);

    private static <T extends MountedItemStorageType<?>> RegistryEntry<MountedItemStorageType<?>, T> simpleItem(String name, Supplier<T> supplier) {
        return REGISTRATE.mountedItemStorage(name, supplier).register();
    }

    public static void register() {}

}

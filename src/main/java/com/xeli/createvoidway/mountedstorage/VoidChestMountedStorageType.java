package com.xeli.createvoidway.mountedstorage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.xeli.createvoidway.blocks.voidtypes.chest.AbstractVoidChestTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VoidChestMountedStorageType extends MountedItemStorageType<VoidChestMountedStorage> {

    public VoidChestMountedStorageType() {
        super(VoidChestMountedStorage.CODEC.fieldOf("data"));
    }

    public @Nullable VoidChestMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof AbstractVoidChestTileEntity voidChest) {
            return VoidChestMountedStorage.fromVoidChest(voidChest);
        } else return null;
    }

}

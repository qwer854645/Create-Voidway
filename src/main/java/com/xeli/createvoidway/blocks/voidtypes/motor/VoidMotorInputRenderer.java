package com.xeli.createvoidway.blocks.voidtypes.motor;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class VoidMotorInputRenderer extends DirectedVoidMotorRenderer<VoidMotorInputTileEntity> {

	public VoidMotorInputRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRenderFrame(VoidMotorInputTileEntity te, Direction direction) {
		return VoidMotorInputBlock.getLinkSlotFace(te.getBlockState()) == direction;
	}

}

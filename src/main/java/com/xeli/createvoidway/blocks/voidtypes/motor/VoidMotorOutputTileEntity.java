package com.xeli.createvoidway.blocks.voidtypes.motor;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.xeli.createvoidway.VoidwayMod;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * Voidway stress source with adjustable RPM, capped by channel stress from paired inputs.
 */
public class VoidMotorOutputTileEntity extends GeneratingKineticBlockEntity
		implements IVoidMotorRelay, IHaveGoggleInformation {

	public static final int DEFAULT_SPEED = 16;
	public static final int MAX_SPEED = 256;
	public static final float OUTPUT_STRESS_CAPACITY = 4.0f;

	protected VoidMotorLinkBehaviour link;
	protected int linkedPartners;
	protected float channelStressTotal;
	protected float channelStressUsed;
	private float grantedRpm;

	public KineticScrollValueBehaviour generatedSpeed;

	public VoidMotorOutputTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);

		generatedSpeed = new KineticScrollValueBehaviour(
				CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
				this, new VoidMotorOutputValueBox());
		generatedSpeed.between(-MAX_SPEED, MAX_SPEED);
		generatedSpeed.value = DEFAULT_SPEED;
		generatedSpeed.withCallback(i -> notifyRelayNetwork());
		behaviours.add(generatedSpeed);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						s -> s.getValue(DirectionalKineticBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));
		link = new VoidMotorLinkBehaviour(this, slots);
	}

	@Override
	public boolean isVoidMotorOutput() {
		return true;
	}

	@Override
	public VoidMotorLinkBehaviour getVoidMotorLink() {
		return link;
	}

	@Override
	public float getChannelStressContribution() {
		return 0;
	}

	@Override
	public float getRequestedChannelStress() {
		if (generatedSpeed == null)
			return 0;
		return (float) Math.floor(OUTPUT_STRESS_CAPACITY * Math.abs(generatedSpeed.getValue()));
	}

	public int getRequestedRpm() {
		return generatedSpeed == null ? 0 : generatedSpeed.getValue();
	}

	public float getGrantedRpm() {
		return grantedRpm;
	}

	@Override
	public boolean isRelayAlive() {
		return level != null && !isRemoved() && level.isLoaded(worldPosition) && level.getBlockEntity(worldPosition) == this;
	}

	@Override
	public int getLinkedPartners() {
		return linkedPartners;
	}

	@Override
	public void setChannelStressStats(float total, float usedStress) {
		if (channelStressTotal == total && channelStressUsed == usedStress)
			return;
		channelStressTotal = total;
		channelStressUsed = usedStress;
		sendData();
	}

	@Override
	public void setLinkedPartners(int partners) {
		if (linkedPartners == partners)
			return;
		linkedPartners = partners;
		sendData();
	}

	@Override
	public void updateLinkedPartnerCount(LevelAccessor world) {
		if (level == null || level.isClientSide)
			return;
		int partners = getHandler().countLinkedPartners(world, link, false);
		if (partners == linkedPartners)
			return;
		linkedPartners = partners;
		sendData();
	}

	private VoidMotorNetworkHandler getHandler() {
		return com.xeli.createvoidway.VoidwayMod.VOID_MOTOR_LINK_NETWORK_HANDLER;
	}

	protected void notifyRelayNetwork() {
		if (level != null && !level.isClientSide && link != null)
			getHandler().updateNetworkOf(level, link);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (level != null && !level.isClientSide)
			notifyRelayNetwork();
	}

	@Override
	public float getGeneratedSpeed() {
		if (grantedRpm == 0)
			return 0;
		return convertToDirection(grantedRpm, getBlockState().getValue(DirectionalKineticBlock.FACING));
	}

	@Override
	public void applyGrantedSpeed(float rpm) {
		if (level == null || level.isClientSide)
			return;
		if (Math.abs(grantedRpm - rpm) < 1e-4f)
			return;
		grantedRpm = rpm;
		updateGeneratedRotation();
	}

	@Override
	public void clearChannelStress() {
		grantedRpm = 0;
		channelStressTotal = 0;
		channelStressUsed = 0;
		if (level == null || level.isClientSide)
			return;
		updateGeneratedRotation();
		sendData();
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;
		if (level.getGameTime() % 20 == 0)
			notifyRelayNetwork();
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		linkedPartners = tag.getInt("LinkedPartners");
		channelStressTotal = tag.getFloat("ChannelStressTotal");
		channelStressUsed = tag.getFloat("ChannelStressUsed");
		grantedRpm = tag.getFloat("GrantedRpm");
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		tag.putInt("LinkedPartners", linkedPartners);
		tag.putFloat("ChannelStressTotal", channelStressTotal);
		tag.putFloat("ChannelStressUsed", channelStressUsed);
		tag.putFloat("GrantedRpm", grantedRpm);
		super.write(tag, registries, clientPacket);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_output.role")
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate("void_motor_output.linked_inputs", linkedPartners)
				.forGoggles(tooltip);
		if (linkedPartners == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor.not_linked")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor.channel_stress_total", (int) channelStressTotal)
					.forGoggles(tooltip);
			new LangBuilder(VoidwayMod.ID)
					.translate("void_motor_output.requested_rpm", getRequestedRpm())
					.forGoggles(tooltip);
			if (Math.abs(grantedRpm) < Math.abs(getRequestedRpm()) && getRequestedRpm() != 0) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_motor_output.stress_limited", (int) Math.abs(grantedRpm))
						.style(ChatFormatting.RED)
						.forGoggles(tooltip);
			}
		}
		return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
	}

}

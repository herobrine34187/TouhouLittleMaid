package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.advancements.maid.TriggerType;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidSchedule;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.SchedulePos;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MaidConfigMessage {
    private final int id;
    private final boolean home;
    private final boolean pick;
    private final boolean ride;
    private final MaidSchedule schedule;

    public MaidConfigMessage(int id, boolean home, boolean pick, boolean ride, MaidSchedule schedule) {
        this.id = id;
        this.home = home;
        this.pick = pick;
        this.ride = ride;
        this.schedule = schedule;
    }

    public static void encode(MaidConfigMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.id);
        buf.writeBoolean(message.home);
        buf.writeBoolean(message.pick);
        buf.writeBoolean(message.ride);
        buf.writeEnum(message.schedule);
    }

    public static MaidConfigMessage decode(FriendlyByteBuf buf) {
        return new MaidConfigMessage(buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readEnum(MaidSchedule.class));
    }

    public static void handle(MaidConfigMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender == null) {
                    return;
                }
                Entity entity = sender.level.getEntity(message.id);
                if (entity instanceof EntityMaid maid && maid.isOwnedBy(sender)) {
                    if (maid.isHomeModeEnable() != message.home) {
                        handleHome(message, sender, maid);
                    }
                    if (maid.isPickup() != message.pick) {
                        maid.setPickup(message.pick);
                    }
                    if (maid.isRideable() != message.ride) {
                        maid.setRideable(message.ride);
                        Entity vehicle = maid.getVehicle();
                        if (!message.ride && vehicle != null && !isStopRideBlocklist(vehicle)) {
                            maid.stopRiding();
                        }
                    }
                    if (maid.getSchedule() != message.schedule) {
                        maid.setSchedule(message.schedule);
                        maid.getSchedulePos().restrictTo(maid);
                        if (maid.isHomeModeEnable()) {
                            BehaviorUtils.setWalkAndLookTargetMemories(maid, maid.getRestrictCenter(), 0.7f, 3);
                        }
                        if (maid.getOwner() instanceof ServerPlayer serverPlayer) {
                            InitTrigger.MAID_EVENT.trigger(serverPlayer, TriggerType.SWITCH_SCHEDULE);
                        }
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }

    private static boolean isStopRideBlocklist(Entity vehicle) {
        // 娱乐方块骑乘不受影响
        boolean isSit = vehicle instanceof EntitySit;
        // 飞行中的扫帚不能脱离，有风险
        boolean isBroom = vehicle instanceof EntityBroom broom && !broom.onGround();
        return isSit || isBroom;
    }

    private static void handleHome(MaidConfigMessage message, ServerPlayer sender, EntityMaid maid) {
        if (message.home) {
            SchedulePos schedulePos = maid.getSchedulePos();
            if (schedulePos.isConfigured()) {
                ResourceLocation dimension = schedulePos.getDimension();
                if (!dimension.equals(maid.level.dimension().location())) {
                    CheckSchedulePosMessage tips = new CheckSchedulePosMessage(Component.translatable("message.touhou_little_maid.check_schedule_pos.dimension"));
                    NetworkHandler.sendToClientPlayer(tips, sender);
                    return;
                }
                BlockPos nearestPos = schedulePos.getNearestPos(maid);
                if (nearestPos != null && nearestPos.distSqr(maid.blockPosition()) > 32 * 32) {
                    CheckSchedulePosMessage tips = new CheckSchedulePosMessage(Component.translatable("message.touhou_little_maid.check_schedule_pos.too_far"));
                    NetworkHandler.sendToClientPlayer(tips, sender);
                    return;
                }
            }
            schedulePos.setHomeModeEnable(maid, maid.blockPosition());
        } else {
            maid.restrictTo(BlockPos.ZERO, MaidConfig.MAID_NON_HOME_RANGE.get());
        }
        maid.setHomeModeEnable(message.home);
    }
}

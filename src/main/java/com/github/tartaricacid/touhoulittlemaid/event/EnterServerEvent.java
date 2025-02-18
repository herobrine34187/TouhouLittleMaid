package com.github.tartaricacid.touhoulittlemaid.event;


import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.SyncAiSettingMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class EnterServerEvent {
    @SubscribeEvent
    public static void onAttachCapabilityEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            InitTrigger.GIVE_SMART_SLAB_CONFIG.trigger(serverPlayer);
            InitTrigger.GIVE_PATCHOULI_BOOK_CONFIG.trigger(serverPlayer);
            NetworkHandler.sendToClientPlayer(new SyncAiSettingMessage(), serverPlayer);
        }
    }
}

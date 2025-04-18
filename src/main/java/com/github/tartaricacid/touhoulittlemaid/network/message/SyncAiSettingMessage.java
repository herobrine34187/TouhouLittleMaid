package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.client.event.PressAIChatKeyEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai.AIChatScreen;
import com.github.tartaricacid.touhoulittlemaid.util.ByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SyncAiSettingMessage {
    private final Set<String> settings;
    private final Map<String, List<String>> chatSites;
    private final Map<String, List<String>> ttsSites;
    private final Set<String> sttSites;

    public SyncAiSettingMessage() {
        this.settings = SettingReader.getAllSettingKeys();
        this.chatSites = AvailableSites.getClientChatSites();
        this.ttsSites = AvailableSites.getClientTtsSites();
        this.sttSites = AvailableSites.getClientSttSites();
    }

    public SyncAiSettingMessage(Set<String> settings, Map<String, List<String>> chatSites, Map<String, List<String>> ttsSites, Set<String> sttSites) {
        this.settings = settings;
        this.chatSites = chatSites;
        this.ttsSites = ttsSites;
        this.sttSites = sttSites;
    }

    public static void encode(SyncAiSettingMessage message, FriendlyByteBuf buf) {
        ByteBufUtils.writeStringSet(message.settings, buf);
        ByteBufUtils.writeSites(message.chatSites, buf);
        ByteBufUtils.writeSites(message.ttsSites, buf);
        ByteBufUtils.writeStringSet(message.sttSites, buf);
    }

    public static SyncAiSettingMessage decode(FriendlyByteBuf buf) {
        Set<String> settings = ByteBufUtils.readStringSet(buf);
        Map<String, List<String>> chatSites = ByteBufUtils.readSites(buf);
        Map<String, List<String>> ttsSites = ByteBufUtils.readSites(buf);
        Set<String> sttSites = ByteBufUtils.readStringSet(buf);
        return new SyncAiSettingMessage(settings, chatSites, ttsSites, sttSites);
    }

    public static void handle(SyncAiSettingMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SyncAiSettingMessage message) {
        PressAIChatKeyEvent.CAN_CHAT_MAID_IDS.clear();
        PressAIChatKeyEvent.CAN_CHAT_MAID_IDS.addAll(message.settings);
        AIChatScreen.CLIENT_CHAT_SITES.clear();
        AIChatScreen.CLIENT_CHAT_SITES.putAll(message.chatSites);
        AIChatScreen.CLIENT_TTS_SITES.clear();
        AIChatScreen.CLIENT_TTS_SITES.putAll(message.ttsSites);
        AIChatScreen.CLIENT_STT_SITES.clear();
        AIChatScreen.CLIENT_STT_SITES.addAll(message.sttSites);
    }
}

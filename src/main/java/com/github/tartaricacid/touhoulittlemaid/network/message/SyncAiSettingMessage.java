package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.client.event.PressAIChatKeyEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncAiSettingMessage {
    private final Set<String> settings;

    public SyncAiSettingMessage() {
        this.settings = SettingReader.getAllSettingKeys();
    }

    public SyncAiSettingMessage(Set<String> settings) {
        this.settings = settings;
    }

    public static void encode(SyncAiSettingMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.settings.size());
        for (String setting : message.settings) {
            buf.writeUtf(setting);
        }
    }

    public static SyncAiSettingMessage decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<String> settings = new HashSet<>();
        for (int i = 0; i < size; i++) {
            settings.add(buf.readUtf());
        }
        return new SyncAiSettingMessage(settings);
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
    }
}

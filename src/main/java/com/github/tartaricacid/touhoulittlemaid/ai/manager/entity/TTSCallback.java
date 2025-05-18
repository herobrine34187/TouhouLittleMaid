package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ErrorCode;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ResponseCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ServiceType;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.TTSAudioToClientMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.net.http.HttpRequest;

public class TTSCallback implements ResponseCallback<byte[]> {
    private final EntityMaid maid;
    private final String chatText;

    public TTSCallback(EntityMaid maid, String chatText) {
        this.maid = maid;
        this.chatText = chatText;
    }

    @Override
    public void onFailure(HttpRequest request, Throwable throwable, int errorCode) {
        if (maid.level instanceof ServerLevel serverLevel) {
            ChatBubbleManger.addAiChatText(maid, chatText);
            MinecraftServer server = serverLevel.getServer();
            server.submit(() -> {
                if (maid.getOwner() instanceof ServerPlayer player) {
                    String cause = throwable.getLocalizedMessage();
                    MutableComponent errorMessage = ErrorCode.getErrorMessage(ServiceType.TTS, errorCode, cause);
                    player.sendSystemMessage(errorMessage.withStyle(ChatFormatting.RED));
                }
            });
        }
        TouhouLittleMaid.LOGGER.error("LLM request failed: {}, error is {}", request, throwable.getMessage());
    }

    @Override
    public void onSuccess(byte[] data) {
        if (!(maid.level instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity owner = maid.getOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        server.submit(() -> {
            NetworkHandler.sendToClientPlayer(new TTSAudioToClientMessage(this.maid.getId(), data), player);
            ChatBubbleManger.addAiChatText(maid, chatText);
        });
    }
}

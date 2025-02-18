package com.github.tartaricacid.touhoulittlemaid.ai.service;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.HistoryChat;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.CharacterSetting;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.TTSClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.Format;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.OpusBitRate;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.TTSRequest;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.ChatClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.ResponseFormat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.Role;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.ApiKeyManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import com.google.gson.Gson;

import java.net.http.HttpClient;
import java.time.Duration;

public final class Service {
    public static final Gson GSON = new Gson();
    // TODO: 增加代理功能？
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public static ChatClient getChatClient() {
        String chatApiKey = ApiKeyManager.getChatApiKey();
        String chatBaseUrl = AIConfig.CHAT_BASE_URL.get();
        return ChatClient.create(HTTP_CLIENT)
                .apiKey(chatApiKey)
                .baseUrl(chatBaseUrl);
    }

    public static ChatCompletion getChatCompletion(EntityMaid maid, String model, String language, CappedQueue<HistoryChat> history) {
        // 获取设定文件
        String setting = CharacterSetting.getSetting(maid, language);

        // 构建对话
        ChatCompletion chatCompletion = ChatCompletion.create()
                .model(model)
                // 温度的范围是 [0,2)
                .temperature(Math.min(AIConfig.CHAT_TEMPERATURE.get(), 1.99))
                .setResponseFormat(ResponseFormat.json())
                .systemChat(setting);

        // 倒序遍历，将历史对话加载进去
        history.getDeque().descendingIterator().forEachRemaining(historyChat -> {
            Role role = historyChat.role();
            String message = historyChat.message();
            if (role.equals(Role.USER)) {
                chatCompletion.userChat(message);
            } else if (role.equals(Role.ASSISTANT)) {
                chatCompletion.assistantChat(message);
            }
        });

        return chatCompletion;
    }

    public static TTSClient getTtsClient() {
        String ttsApiKey = ApiKeyManager.getTtsApiKey();
        String ttsBaseUrl = AIConfig.TTS_BASE_URL.get();
        return TTSClient.create(HTTP_CLIENT)
                .apiKey(ttsApiKey)
                .baseUrl(ttsBaseUrl);
    }

    public static TTSRequest getTtsRequest(String model, String text) {
        return TTSRequest.create()
                .setReferenceId(model)
                .setFormat(Format.OPUS)
                // OPUS 极低比特率情况下，音质效果也还不错
                .setOpusBitrate(OpusBitRate.LOWEST)
                .setText(text);
    }
}

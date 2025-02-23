package com.github.tartaricacid.touhoulittlemaid.ai.service;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.HistoryChat;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.Site;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.TTSClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.Format;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.OpusBitRate;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.TTSRequest;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.ChatClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.ResponseFormat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.Role;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpClient;
import java.time.Duration;

public final class Service {
    public static final Gson GSON = new Gson();
    // TODO: 增加代理功能？
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public static ChatClient getChatClient(Site site) {
        String chatApiKey = site.getApiKey();
        String chatBaseUrl = site.getUrl();
        return ChatClient.create(HTTP_CLIENT)
                .apiKey(chatApiKey)
                .baseUrl(chatBaseUrl);
    }

    @Nullable
    public static ChatCompletion getChatCompletion(MaidAIChatManager chatManager, String language) {
        // 获取设定文件
        return chatManager.getSetting().map(s -> {
            String setting = s.getSetting(chatManager.getMaid(), language);
            String model = chatManager.getChatModel();
            double chatTemperature = chatManager.getChatTemperature();
            CappedQueue<HistoryChat> history = chatManager.getHistory();

            // 构建对话
            ChatCompletion chatCompletion = ChatCompletion.create()
                    .model(model)
                    .temperature(chatTemperature)
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
        }).orElse(null);
    }

    public static TTSClient getTtsClient(Site site) {
        String ttsApiKey = site.getApiKey();
        String ttsBaseUrl = site.getUrl();
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

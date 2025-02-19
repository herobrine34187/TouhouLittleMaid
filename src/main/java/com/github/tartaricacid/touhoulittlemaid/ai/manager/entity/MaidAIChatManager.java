package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.CharacterSetting;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.Site;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Service;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.TTSClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.request.TTSRequest;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.ChatClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.ai.service.openai.response.ChatCompletionResponse;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.TTSAudioToClientMessage;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class MaidAIChatManager {
    private final EntityMaid maid;
    private final CappedQueue<HistoryChat> history;

    private String chatSite = "";
    private String chatModel = "";
    private String ttsSite = "";
    private String ttsModel = "";

    public MaidAIChatManager(EntityMaid maid) {
        this.maid = maid;
        this.history = new CappedQueue<>(AIConfig.MAID_MAX_HISTORY_CHAT_SIZE.get());
    }

    public void chat(String message, String language) {
        if (AIConfig.CHAT_ENABLED.get()) {
            @Nullable Site site = this.getChatSite();
            if (site == null || StringUtils.isBlank(site.getApiKey())) {
                ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.api_key.empty");
            } else {
                ChatClient chatClient = Service.getChatClient(site);
                ChatCompletion chatCompletion = Service.getChatCompletion(this, language);
                if (chatCompletion != null) {
                    chatCompletion.userChat(message);
                    chatClient.chat(chatCompletion).handle(this::onShowChatSync);
                    this.addUserHistory(message);
                } else {
                    ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.no_setting");
                }
            }
        } else {
            ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.disable");
        }
    }

    public String getChatModel() {
        Site site = getChatSite();
        String model = StringUtils.EMPTY;
        if (site != null && !site.getModels().isEmpty()) {
            if (StringUtils.isBlank(chatModel)) {
                model = site.getModels().get(0);
            } else {
                model = chatModel;
            }
        }
        return model;
    }

    public String getTtsModel() {
        Site site = getTtsSite();
        String model = StringUtils.EMPTY;
        if (site != null && !site.getModels().isEmpty()) {
            if (StringUtils.isBlank(ttsModel)) {
                model = site.getModels().get(0);
            } else {
                model = ttsModel;
            }
        }
        return model;
    }

    public CappedQueue<HistoryChat> getHistory() {
        return history;
    }

    public EntityMaid getMaid() {
        return maid;
    }

    public Optional<CharacterSetting> getSetting() {
        String modelId = this.maid.getModelId();
        return SettingReader.getSetting(modelId);
    }

    public void setChatSite(String chatSite) {
        this.chatSite = chatSite;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public void setTtsSite(String ttsSite) {
        this.ttsSite = ttsSite;
    }

    public void setTtsModel(String ttsModel) {
        this.ttsModel = ttsModel;
    }

    @Nullable
    private Site getChatSite() {
        Site site;
        if (StringUtils.isBlank(chatSite)) {
            site = AvailableSites.getFirstAvailableChatSite();
        } else {
            site = AvailableSites.getChatSite(chatSite);
            if (site == null) {
                site = AvailableSites.getFirstAvailableChatSite();
            }
        }
        return site;
    }

    @Nullable
    private Site getTtsSite() {
        Site site;
        if (StringUtils.isBlank(ttsSite)) {
            site = AvailableSites.getFirstAvailableTtsSite();
        } else {
            site = AvailableSites.getTtsSite(ttsSite);
            if (site == null) {
                site = AvailableSites.getFirstAvailableTtsSite();
            }
        }
        return site;
    }

    private void tts(Site site, String chatText, String ttsText) {
        TTSClient ttsClient = Service.getTtsClient(site);
        TTSRequest ttsRequest = Service.getTtsRequest(this.getTtsModel(), ttsText);
        ttsClient.request(ttsRequest).handle(data -> onPlaySoundSync(chatText, data));
    }

    private void onShowChatSync(ChatCompletionResponse result) {
        String rawMessage = result.getFirstChoiceMessage();
        try {
            ResponseChat responseChat = Service.GSON.fromJson(rawMessage, ResponseChat.class);
            if (responseChat == null) {
                TouhouLittleMaid.LOGGER.error("Error in Response Chat: {}", rawMessage);
                return;
            }
            String chatText = responseChat.getChatText();
            String ttsText = responseChat.getTtsText();
            if (StringUtils.isBlank(chatText) || StringUtils.isBlank(ttsText)) {
                TouhouLittleMaid.LOGGER.error("Error in Response Chat: {}", rawMessage);
                return;
            }
            this.addAssistantHistory(rawMessage);
            Site site = this.getTtsSite();
            if (AIConfig.TTS_ENABLED.get() && site != null && StringUtils.isNotBlank(site.getApiKey())) {
                this.tts(site, chatText, ttsText);
            } else {
                ChatBubbleManger.addAiChatTextSync(maid, chatText);
            }
        } catch (Exception e) {
            TouhouLittleMaid.LOGGER.error(e.getMessage());
        }
    }

    private void onPlaySoundSync(String chatText, byte[] data) {
        if (!(maid.level instanceof ServerLevel serverLevel)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        server.submit(() -> {
            NetworkHandler.sendToNearby(maid, new TTSAudioToClientMessage(this.maid.getId(), data));
            ChatBubbleManger.addAiChatText(maid, chatText);
        });
    }

    private void addUserHistory(String message) {
        this.history.add(HistoryChat.userChat(maid, message));
    }

    private void addAssistantHistory(String message) {
        this.history.add(HistoryChat.assistantChat(maid, message));
    }
}

package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.PapiReplacer;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.StringConstant;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMMessage;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMSite;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSSite;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSSystemServices;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.TTSSystemAudioToClientMessage;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.StringConstant.AUTO_GEN_SETTING;

public final class MaidAIChatManager extends MaidAIChatData {
    public MaidAIChatManager(EntityMaid maid) {
        super(maid);
    }

    public void chat(String message, ChatClientInfo clientInfo) {
        if (!AIConfig.LLM_ENABLED.get()) {
            ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.disable");
        }
        @Nullable LLMSite site = this.getLLMSite();
        if (site == null || !site.enabled()) {
            ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.llm.empty");
            return;
        }
        LLMClient chatClient = site.client();
        List<LLMMessage> chatCompletion = getChatCompletion(this, clientInfo.language());
        if (chatCompletion.isEmpty()) {
            if (AIConfig.AUTO_GEN_SETTING_ENABLED.get()) {
                ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.llm.role_no_setting_and_gen_setting");
                LLMMessage llmMessage = autoGenSetting(maid, clientInfo);
                chatCompletion.add(llmMessage);
                LLMConfig config = new LLMConfig(this.getLLMModel(), AIConfig.LLM_TEMPERATURE.get(), AIConfig.LLM_MAX_TOKEN.get());
                AutoGenSettingCallback callback = new AutoGenSettingCallback(this, message);
                chatClient.chat(this.maid, chatCompletion, config, callback);
            } else {
                ChatBubbleManger.addInnerChatText(maid, "ai.touhou_little_maid.chat.llm.role_no_setting");
            }
        } else {
            chatCompletion.add(LLMMessage.userChat(maid, message));
            LLMConfig config = new LLMConfig(this.getLLMModel(), AIConfig.LLM_TEMPERATURE.get(), AIConfig.LLM_MAX_TOKEN.get());
            LLMCallback callback = new LLMCallback(this, message);
            chatClient.chat(this.maid, chatCompletion, config, callback);
        }
    }

    @SuppressWarnings("all")
    public void tts(TTSSite site, String chatText, String ttsText) {
        // 调用系统 TTS，那么此时就只需要发送给指定的玩家即可
        TTSClient ttsClient = site.client();
        String ttsModel = getTTSModel();

        String ttsLang = "en";
        String[] split = this.getTTSLanguage().split("_");
        if (split.length >= 2) {
            ttsLang = split[0];
        }
        TTSConfig config = new TTSConfig(ttsModel, ttsLang);

        if (ttsClient instanceof TTSSystemServices services) {
            onPlaySoundLocal(site.id(), chatText, ttsText, config, services);
        } else {
            TTSCallback callback = new TTSCallback(maid, chatText);
            ttsClient.play(ttsText, config, callback);
        }
    }

    private static List<LLMMessage> getChatCompletion(MaidAIChatManager chatManager, String language) {
        // 如果含有自定义设定，则直接使用自定义设定
        if (!chatManager.customSetting.isBlank()) {
            EntityMaid maid = chatManager.getMaid();
            String setting = PapiReplacer.replace(chatManager.customSetting, maid, language);
            CappedQueue<LLMMessage> history = chatManager.getHistory();
            List<LLMMessage> chatList = Lists.newArrayList();
            chatList.add(LLMMessage.systemChat(maid, setting));
            // 倒序遍历，将历史对话加载进去
            history.getDeque().descendingIterator().forEachRemaining(chatList::add);
            // 最后强调一下语言类型
            chatList.add(LLMMessage.userChat(maid, StringConstant.SECONDARY_EMPHASIS_LANGUAGE
                    .formatted(language, chatManager.getTTSLanguage())));
            return chatList;
        }

        // 其他情况下，获取默认设定文件
        return chatManager.getSetting().map(s -> {
            EntityMaid maid = chatManager.getMaid();
            String setting = s.getSetting(maid, language);
            CappedQueue<LLMMessage> history = chatManager.getHistory();
            List<LLMMessage> chatList = Lists.newArrayList();
            chatList.add(LLMMessage.systemChat(maid, setting));
            // 倒序遍历，将历史对话加载进去
            history.getDeque().descendingIterator().forEachRemaining(chatList::add);
            // 最后强调一下语言类型
            chatList.add(LLMMessage.userChat(maid, StringConstant.SECONDARY_EMPHASIS_LANGUAGE
                    .formatted(language, chatManager.getTTSLanguage())));
            return chatList;
        }).orElse(Lists.newArrayList());
    }

    private static LLMMessage autoGenSetting(EntityMaid maid, ChatClientInfo clientInfo) {
        Map<String, String> valueMap = Maps.newHashMap();
        valueMap.put("output_json_format", Client.GSON.toJson(new ResponseChat()));
        valueMap.put("model_name", clientInfo.name());
        valueMap.put("chat_language", clientInfo.language());
        String setting = new StrSubstitutor(valueMap).replace(AUTO_GEN_SETTING);

        // 如果有描述文本，那么就将描述文本也加入到设定中
        if (!clientInfo.description().isEmpty()) {
            String join = StringUtils.join(clientInfo.description(), "\n");
            valueMap.put("model_desc", join);
            String desc = new StrSubstitutor(valueMap).replace(StringConstant.AUTO_GEN_SETTING_DESC);
            setting = setting + desc;
        }

        return LLMMessage.userChat(maid, setting);
    }

    private void onPlaySoundLocal(String name, String chatText, String ttsText, TTSConfig config, TTSSystemServices services) {
        if (!(maid.level instanceof ServerLevel serverLevel)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        server.submit(() -> {
            if (maid.getOwner() instanceof ServerPlayer player) {
                TTSSystemAudioToClientMessage message = new TTSSystemAudioToClientMessage(name, ttsText, config, services);
                NetworkHandler.sendToClientPlayer(message, player);
            }
            ChatBubbleManger.addAiChatText(maid, chatText);
        });
    }
}

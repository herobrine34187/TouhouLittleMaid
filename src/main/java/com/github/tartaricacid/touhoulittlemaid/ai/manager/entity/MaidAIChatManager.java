package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.PapiReplacer;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.StringConstant;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.*;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSSite;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSSystemServices;
import com.github.tartaricacid.touhoulittlemaid.capability.ChatTokensCapabilityProvider;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManager;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.TTSSystemAudioToClientMessage;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

    public void chat(String message, ChatClientInfo clientInfo, ServerPlayer sender) {
        if (!AIConfig.LLM_ENABLED.get()) {
            sender.sendSystemMessage(Component.translatable("ai.touhou_little_maid.chat.disable")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        sender.getCapability(ChatTokensCapabilityProvider.CHAT_TOKENS_CAP).ifPresent(chatTokens -> {
            if (chatTokens.getCount() >= AIConfig.MAX_TOKENS_PER_PLAYER.get()) {
                sender.sendSystemMessage(Component.translatable("message.touhou_little_maid.ai_chat.max_tokens_limit")
                        .withStyle(ChatFormatting.RED));
                return;
            }
            @Nullable LLMSite site = this.getLLMSite();
            if (site == null || !site.enabled()) {
                sender.sendSystemMessage(Component.translatable("ai.touhou_little_maid.chat.llm.empty")
                        .withStyle(ChatFormatting.RED));
                return;
            }
            LLMClient chatClient = site.client();
            List<LLMMessage> chatCompletion = getChatCompletion(this, clientInfo.language());
            if (chatCompletion.isEmpty()) {
                this.onSettingIsEmpty(message, clientInfo, chatCompletion, chatClient);
            } else {
                this.normalChat(message, chatCompletion, chatClient);
            }
        });
    }

    private void normalChat(String message, List<LLMMessage> chatCompletion, LLMClient chatClient) {
        ChatBubbleManager bubbleManager = this.maid.getChatBubbleManager();
        chatCompletion.add(LLMMessage.userChat(maid, message));
        LLMConfig config = LLMConfig.normalChat(this.getLLMModel(), this.maid);
        Component thinkTip = Component.translatable("ai.touhou_little_maid.chat.chat_bubble_waiting")
                .withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
        long key = bubbleManager.addChatBubble(TextChatBubbleData.type2(thinkTip));
        LLMCallback callback = new LLMCallback(this, message, key);
        chatClient.chat(chatCompletion, config, callback);
    }

    private void onSettingIsEmpty(String message, ChatClientInfo clientInfo, List<LLMMessage> chatCompletion, LLMClient chatClient) {
        ChatBubbleManager bubbleManager = this.maid.getChatBubbleManager();
        if (AIConfig.AUTO_GEN_SETTING_ENABLED.get()) {
            LLMMessage llmMessage = autoGenSetting(maid, clientInfo);
            chatCompletion.add(llmMessage);
            LLMConfig config = new LLMConfig(this.getLLMModel(), this.maid, ChatType.AUTO_GEN_SETTING);
            long key = bubbleManager.addTextChatBubble("ai.touhou_little_maid.chat.llm.role_no_setting_and_gen_setting");
            AutoGenSettingCallback callback = new AutoGenSettingCallback(this, message, key);
            chatClient.chat(chatCompletion, config, callback);
        } else {
            bubbleManager.addTextChatBubble("ai.touhou_little_maid.chat.llm.role_no_setting");
        }
    }

    @SuppressWarnings("all")
    public void tts(TTSSite site, String chatText, String ttsText, long waitingChatBubbleId) {
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
            onPlaySoundLocal(site.id(), chatText, ttsText, config, services, waitingChatBubbleId);
        } else {
            TTSCallback callback = new TTSCallback(maid, chatText, waitingChatBubbleId);
            ttsClient.play(ttsText, config, callback);
        }
    }

    private List<LLMMessage> getChatCompletion(MaidAIChatManager chatManager, String language) {
        // 如果含有自定义设定，则直接使用自定义设定
        if (StringUtils.isNotBlank(chatManager.customSetting)) {
            EntityMaid maid = chatManager.getMaid();
            String setting = PapiReplacer.replace(chatManager.customSetting, maid, language);
            CappedQueue<LLMMessage> history = chatManager.getHistory();
            List<LLMMessage> chatList = Lists.newArrayList();
            chatList.add(LLMMessage.systemChat(maid, setting));
            // 倒序遍历，将历史对话加载进去
            history.getDeque().descendingIterator().forEachRemaining(chatList::add);
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
            return chatList;
        }).orElse(Lists.newArrayList());
    }

    private LLMMessage autoGenSetting(EntityMaid maid, ChatClientInfo clientInfo) {
        Map<String, String> valueMap = Maps.newHashMap();
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

    private void onPlaySoundLocal(String name, String chatText, String ttsText, TTSConfig config, TTSSystemServices services, long waitingChatBubbleId) {
        if (!(maid.level instanceof ServerLevel serverLevel)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        server.submit(() -> {
            if (maid.getOwner() instanceof ServerPlayer player) {
                TTSSystemAudioToClientMessage message = new TTSSystemAudioToClientMessage(name, ttsText, config, services);
                NetworkHandler.sendToClientPlayer(message, player);
            }
            maid.getChatBubbleManager().addLLMChatText(chatText, waitingChatBubbleId);
        });
    }
}

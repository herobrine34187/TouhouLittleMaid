package com.github.tartaricacid.touhoulittlemaid.compat.cloth;

import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.ApiKeyManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.SortedMap;

public class AIChatIntegration {
    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder root = ConfigBuilder.create().setTitle(Component.literal("Touhou Little Maid"));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = root.entryBuilder();
        init(root, entryBuilder);
        return root;
    }

    public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        aiChat(root, entryBuilder);
        tts(root, entryBuilder);
        apiKey(root, entryBuilder);
    }

    private static void aiChat(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory aiChat = root.getOrCreateCategory(Component.translatable("config.touhou_little_maid.ai_chat"));

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.ai_chat.chat_enable"), AIConfig.CHAT_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.ai_chat.chat_enable.tooltip"))
                .setSaveConsumer(AIConfig.CHAT_ENABLED::set).build());

        aiChat.addEntry(entryBuilder.startStrField(Component.translatable("config.touhou_little_maid.ai_chat.chat_base_url"), AIConfig.CHAT_BASE_URL.get())
                .setDefaultValue(AIConfig.CHAT_BASE_URL.getDefault()).setTooltip(Component.translatable("config.touhou_little_maid.ai_chat.chat_base_url.tooltip"))
                .setSaveConsumer(AIConfig.CHAT_BASE_URL::set).build());

        aiChat.addEntry(entryBuilder.startStrField(Component.translatable("config.touhou_little_maid.ai_chat.chat_model"), AIConfig.CHAT_MODEL.get())
                .setDefaultValue(AIConfig.CHAT_MODEL.getDefault()).setTooltip(Component.translatable("config.touhou_little_maid.ai_chat.chat_model.tooltip"))
                .setSaveConsumer(AIConfig.CHAT_MODEL::set).build());

        aiChat.addEntry(entryBuilder.startDoubleField(Component.translatable("config.touhou_little_maid.ai_chat.chat_temperature"), AIConfig.CHAT_TEMPERATURE.get())
                .setDefaultValue(AIConfig.CHAT_TEMPERATURE.getDefault()).setMin(0.0).setMax(2.0)
                .setTooltip(Component.translatable("config.touhou_little_maid.ai_chat.chat_temperature.tooltip"))
                .setSaveConsumer(AIConfig.CHAT_TEMPERATURE::set).build());
    }

    private static void tts(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory tts = root.getOrCreateCategory(Component.translatable("config.touhou_little_maid.tts"));

        tts.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.tts.tts_enable"), AIConfig.TTS_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.tts.tts_enable.tooltip"))
                .setSaveConsumer(AIConfig.TTS_ENABLED::set).build());

        tts.addEntry(entryBuilder.startStrField(Component.translatable("config.touhou_little_maid.tts.tts_base_url"), AIConfig.TTS_BASE_URL.get())
                .setDefaultValue(AIConfig.TTS_BASE_URL.getDefault()).setTooltip(Component.translatable("config.touhou_little_maid.tts.tts_base_url.tooltip"))
                .setSaveConsumer(AIConfig.TTS_BASE_URL::set).build());

        tts.addEntry(entryBuilder.startStrField(Component.translatable("config.touhou_little_maid.tts.tts_model"), AIConfig.TTS_MODEL.get())
                .setDefaultValue(AIConfig.TTS_MODEL.getDefault()).setTooltip(Component.translatable("config.touhou_little_maid.tts.tts_model.tooltip"))
                .setSaveConsumer(AIConfig.TTS_MODEL::set).build());

        ttsLanguage(tts, entryBuilder);
    }

    private static void apiKey(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory category = root.getOrCreateCategory(Component.translatable("config.touhou_little_maid.api_key"));
        SubCategoryBuilder apiKey = entryBuilder.startSubCategory(Component.translatable("config.touhou_little_maid.api_key.secret"))
                .setTooltip(Component.translatable("config.touhou_little_maid.api_key.secret.tooltip"))
                .setExpanded(false);

        apiKey.add(entryBuilder.startStrField(Component.translatable("config.touhou_little_maid.api_key.chat"), ApiKeyManager.getChatApiKey())
                .setDefaultValue(StringUtils.EMPTY).setTooltip(Component.translatable("config.touhou_little_maid.api_key.chat.tooltip"))
                .setSaveConsumer(ApiKeyManager::setChatApiKey).build());

        apiKey.add(entryBuilder.startStrField(Component.translatable("config.touhou_little_maid.api_key.tts"), ApiKeyManager.getTtsApiKey())
                .setDefaultValue(StringUtils.EMPTY).setTooltip(Component.translatable("config.touhou_little_maid.api_key.tts.tooltip"))
                .setSaveConsumer(ApiKeyManager::setTtsApiKey).build());

        category.addEntry(apiKey.build());
    }

    private static void ttsLanguage(ConfigCategory tts, ConfigEntryBuilder entryBuilder) {
        SortedMap<String, LanguageInfo> languages = Minecraft.getInstance().getLanguageManager().getLanguages();
        tts.addEntry(entryBuilder.startStringDropdownMenu(Component.translatable("config.touhou_little_maid.tts.tts_language"),
                        AIConfig.TTS_LANGUAGE.get(), Component::literal, cell(languages)).setSelections(languages.keySet())
                .setDefaultValue(LanguageManager.DEFAULT_LANGUAGE_CODE).setTooltip(Component.translatable("config.touhou_little_maid.tts.tts_language.tooltip"))
                .setSaveConsumer(info -> AIConfig.TTS_LANGUAGE.set(info)).build());
    }

    private static DropdownBoxEntry.SelectionCellCreator<String> cell(SortedMap<String, LanguageInfo> languages) {
        LanguageInfo defaultLanguage = languages.get(LanguageManager.DEFAULT_LANGUAGE_CODE);
        return new DropdownBoxEntry.DefaultSelectionCellCreator<>(i -> languages.getOrDefault(i, defaultLanguage).toComponent());
    }
}

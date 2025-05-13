package com.github.tartaricacid.touhoulittlemaid.compat.cloth;

import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;

import java.util.SortedMap;

public class GlobalAIIntegration {
    public static void aiChat(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory aiChat = root.getOrCreateCategory(Component.translatable("config.touhou_little_maid.global_ai"));

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.llm_enable"), AIConfig.LLM_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.llm_enable.tooltip"))
                .setSaveConsumer(AIConfig.LLM_ENABLED::set).build());

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.tts_enable"), AIConfig.TTS_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.tts_enable.tooltip"))
                .setSaveConsumer(AIConfig.TTS_ENABLED::set).build());

        aiChat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.touhou_little_maid.global_ai.stt_enable"), AIConfig.STT_ENABLED.get())
                .setDefaultValue(true).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.stt_enable.tooltip"))
                .setSaveConsumer(AIConfig.STT_ENABLED::set).build());

        aiChat.addEntry(entryBuilder.startDoubleField(Component.translatable("config.touhou_little_maid.global_ai.llm_temperature"), AIConfig.LLM_TEMPERATURE.get())
                .setDefaultValue(AIConfig.LLM_TEMPERATURE.getDefault()).setMin(0.0).setMax(2.0)
                .setTooltip(Component.translatable("config.touhou_little_maid.global_ai.llm_temperature.tooltip"))
                .setSaveConsumer(AIConfig.LLM_TEMPERATURE::set).build());

        aiChat.addEntry(entryBuilder.startIntSlider(Component.translatable("config.touhou_little_maid.global_ai.maid_max_history_llm_size"),
                        AIConfig.MAID_MAX_HISTORY_LLM_SIZE.get(), 1, 128).setDefaultValue(16)
                .setTooltip(Component.translatable("config.touhou_little_maid.global_ai.maid_max_history_llm_size.tooltip"))
                .setSaveConsumer(AIConfig.MAID_MAX_HISTORY_LLM_SIZE::set).build());

        SortedMap<String, LanguageInfo> languages = Minecraft.getInstance().getLanguageManager().getLanguages();
        aiChat.addEntry(entryBuilder.startStringDropdownMenu(Component.translatable("config.touhou_little_maid.global_ai.tts_language"),
                        AIConfig.TTS_LANGUAGE.get(), Component::literal, cell(languages)).setSelections(languages.keySet())
                .setDefaultValue(LanguageManager.DEFAULT_LANGUAGE_CODE).setTooltip(Component.translatable("config.touhou_little_maid.global_ai.tts_language.tooltip"))
                .setSaveConsumer(info -> AIConfig.TTS_LANGUAGE.set(info)).build());
    }

    private static DropdownBoxEntry.SelectionCellCreator<String> cell(SortedMap<String, LanguageInfo> languages) {
        LanguageInfo defaultLanguage = languages.get(LanguageManager.DEFAULT_LANGUAGE_CODE);
        return new DropdownBoxEntry.DefaultSelectionCellCreator<>(i -> languages.getOrDefault(i, defaultLanguage).toComponent());
    }
}

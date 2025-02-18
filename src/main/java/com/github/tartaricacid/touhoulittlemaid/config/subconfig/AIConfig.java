package com.github.tartaricacid.touhoulittlemaid.config.subconfig;

import net.minecraftforge.common.ForgeConfigSpec;

public class AIConfig {
    public static ForgeConfigSpec.BooleanValue CHAT_ENABLED;
    public static ForgeConfigSpec.ConfigValue<String> CHAT_BASE_URL;
    public static ForgeConfigSpec.ConfigValue<String> CHAT_MODEL;
    public static ForgeConfigSpec.DoubleValue CHAT_TEMPERATURE;

    public static ForgeConfigSpec.BooleanValue TTS_ENABLED;
    public static ForgeConfigSpec.ConfigValue<String> TTS_BASE_URL;
    public static ForgeConfigSpec.ConfigValue<String> TTS_MODEL;
    public static ForgeConfigSpec.ConfigValue<String> TTS_LANGUAGE;

    public static ForgeConfigSpec.IntValue MAID_MAX_HISTORY_CHAT_SIZE;

    public static void init(ForgeConfigSpec.Builder builder) {
        // AI 相关 key 初始化
        // API KEY 需要单独弄个地方写
        ApiKeyManager.getOrCreateApiKey();

        builder.push("ai");

        builder.comment("Whether or not to enable the AI Chat feature");
        CHAT_ENABLED = builder.define("ChatEnabled", true);

        builder.comment("The chat AI base url you intend to use");
        CHAT_BASE_URL = builder.define("ChatBaseURL", "https://api.deepseek.com");

        builder.comment("The chat AI model you intend to use");
        CHAT_MODEL = builder.define("ChatModel", "deepseek-chat");

        builder.comment("Chat temperature, the higher this value, the more random the output will be");
        CHAT_TEMPERATURE = builder.defineInRange("ChatTemperature", 0.5, 0, 2);

        builder.comment("Whether or not to enable the TTS feature");
        TTS_ENABLED = builder.define("TTSEnabled", true);

        builder.comment("The TTS API you intend to use");
        TTS_BASE_URL = builder.define("TTSBaseURL", "https://api.fish-audio.cn/v1");

        builder.comment("The TTS model you intend to use");
        TTS_MODEL = builder.define("TTSModel", "4858e0be678c4449bf3a7646186edd42");

        builder.comment("The TTS language you intend to use");
        TTS_LANGUAGE = builder.define("TTSLanguage", "en_us");

        builder.comment("The maximum historical conversation length cached by the maid");
        MAID_MAX_HISTORY_CHAT_SIZE = builder.defineInRange("MaidMaxHistoryChatSize", 16, 1, Integer.MAX_VALUE);

        builder.pop();
    }
}

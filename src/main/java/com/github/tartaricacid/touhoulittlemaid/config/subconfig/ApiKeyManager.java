package com.github.tartaricacid.touhoulittlemaid.config.subconfig;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.util.SystemAppDataUtil;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 因为有的人总是习惯性分享整合包时，不检查配置文件
 * <p>
 * 所以把 API KEY 写入配置文件非常的不安全，故我们单独写在 app data 里
 * <p>
 * 不要吐槽我在 app data 里丢垃圾，这也是没有办法的事情
 */
public final class ApiKeyManager {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Map<String, String> API_KEYS = Maps.newLinkedHashMap();
    private static final String FILE_NAME = "api_keys.json";
    private static final String CHAT = "chat";
    private static final String TTS = "tts";

    public static void getOrCreateApiKey() {
        try {
            File apiKeyFile = getApiKeyFile();
            if (apiKeyFile.exists()) {
                readExistsFile(apiKeyFile);
            } else {
                createInitFile(apiKeyFile);
            }
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Error on create app data dir: ", e);
        }
    }

    public static String getChatApiKey() {
        return API_KEYS.get(CHAT);
    }

    public static String getTtsApiKey() {
        return API_KEYS.get(TTS);
    }

    public static void setChatApiKey(String apiKey) {
        setApiKeys(CHAT, apiKey);
    }

    public static void setTtsApiKey(String apiKey) {
        setApiKeys(TTS, apiKey);
    }

    private static void setApiKeys(String apiKeyName, String apiKey) {
        API_KEYS.put(apiKeyName, apiKey);
        try (FileWriter writer = new FileWriter(getApiKeyFile(), StandardCharsets.UTF_8)) {
            GSON.toJson(API_KEYS, writer);
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Error in set api_keys.json file: ", e);
        }
    }

    private static void createInitFile(File apiKeyFile) {
        try (FileWriter writer = new FileWriter(apiKeyFile, StandardCharsets.UTF_8)) {
            API_KEYS.put(CHAT, StringUtils.EMPTY);
            API_KEYS.put(TTS, StringUtils.EMPTY);
            GSON.toJson(API_KEYS, writer);
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Error in create api_keys.json file: ", e);
        }
    }

    private static void readExistsFile(File apiKeyFile) {
        try (FileReader reader = new FileReader(apiKeyFile, StandardCharsets.UTF_8)) {
            API_KEYS.putAll(GSON.fromJson(reader, new TypeToken<Map<String, String>>() {
            }));
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Error in read api_keys.json file: ", e);
        }
    }

    @NotNull
    private static File getApiKeyFile() throws IOException {
        return SystemAppDataUtil.getAndCreateAppDataDir(TouhouLittleMaid.MOD_ID).resolve(FILE_NAME).toFile();
    }
}

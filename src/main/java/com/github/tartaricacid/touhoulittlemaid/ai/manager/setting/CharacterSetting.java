package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.GetJarResources;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CharacterSetting {
    private static final String JAR_PRESET_FILE_PATH = "/assets/touhou_little_maid/tlm_custom_pack/ai_chat_setting.toml";
    private static final Path CONFIG_PRESET_FILE_PATH = Paths.get("config", "touhou_little_maid", "ai_chat_setting.toml");
    private static final String SETTING_KEY = "setting";
    private static final String OUTPUT_KEY = "output";
    private static final List<String> SETTING_KEY_ORDER = Lists.newArrayList("base", "name", "favorite", "background", "edge_cases", "context");
    private static final List<String> OUTPUT_KEY_ORDER = Lists.newArrayList("length", "format", "language");
    private static String RAW_SETTING = "";

    public static void readPresetFile() {
        File configFile = null;
        if (CONFIG_PRESET_FILE_PATH.toFile().exists()) {
            configFile = CONFIG_PRESET_FILE_PATH.toFile();
        } else {
            configFile = getJarPersetFile(configFile);
        }
        if (configFile != null) {
            try (FileConfig config = FileConfig.of(configFile)) {
                config.load();
                StringBuilder builder = new StringBuilder();
                getConfigText(config.get(SETTING_KEY), builder, SETTING_KEY_ORDER);
                getConfigText(config.get(OUTPUT_KEY), builder, OUTPUT_KEY_ORDER);
                RAW_SETTING = builder.toString();
            }
        }
    }

    public static String getSetting(EntityMaid maid, String language) {
        return PapiReplacer.replace(RAW_SETTING, maid, language);
    }

    private static File getJarPersetFile(File configFile) {
        try (InputStream inputStream = GetJarResources.readTouhouLittleMaidFile(JAR_PRESET_FILE_PATH)) {
            if (inputStream != null) {
                // TOML 库只支持文件直接读取，不支持文件流，还得先写成临时文件才可以
                configFile = Files.createTempFile("touhou_little_maid", ".toml").toFile();
                FileUtils.copyInputStreamToFile(inputStream, configFile);
                configFile.deleteOnExit();
            }
        } catch (Exception e) {
            TouhouLittleMaid.LOGGER.error("Failed to read ai_chat_setting.toml", e);
        }
        return configFile;
    }

    private static void getConfigText(Config config, StringBuilder builder, List<String> knownOrder) {
        Set<String> keySet = config.valueMap().keySet();
        List<String> orderKeySet = sortSetByKnownOrder(keySet, knownOrder);
        for (String key : orderKeySet) {
            String text = config.get(key);
            processText(text).forEach(value -> builder.append(value).append("\n"));
        }
    }

    private static List<String> processText(String text) {
        return Arrays.stream(StringUtils.split(text, "\n"))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    private static List<String> sortSetByKnownOrder(Set<String> set, List<String> knownOrder) {
        List<String> sortedList = new ArrayList<>(set);
        sortedList.sort(Comparator.comparingInt(knownOrder::indexOf));
        return sortedList;
    }
}
package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.util.GetJarResources;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class AvailableSites {
    public static final String FILE_NAME = "available_sites.yml";
    public static final Map<String, Site> CHAT_SITES = Maps.newLinkedHashMap();
    public static final Map<String, Site> TTS_SITES = Maps.newLinkedHashMap();
    private static final Path SITES_FILES = Paths.get("config", TouhouLittleMaid.MOD_ID, FILE_NAME);
    private static final String JAR_SITES_FILES = String.format("/assets/%s/config/%s", TouhouLittleMaid.MOD_ID, FILE_NAME);

    public static void readSites() {
        CHAT_SITES.clear();
        TTS_SITES.clear();
        Yaml yaml = new Yaml();
        Map<String, LinkedHashMap<String, Object>> allSites = Maps.newLinkedHashMap();

        // 先尝试读取 jar 包内的文件
        try (InputStream stream = GetJarResources.readTouhouLittleMaidFile(JAR_SITES_FILES)) {
            allSites.putAll(yaml.load(stream));
        } catch (Exception e) {
            TouhouLittleMaid.LOGGER.error("Failed to read available sites jar file", e);
        }

        // 再尝试读取配置文件下的内容
        if (SITES_FILES.toFile().isFile()) {
            try (FileReader reader = new FileReader(SITES_FILES.toFile(), StandardCharsets.UTF_8)) {
                allSites.putAll(yaml.load(reader));
            } catch (Exception e) {
                TouhouLittleMaid.LOGGER.error("Failed to read available sites file", e);
            }
        }

        // 分类
        allSites.forEach((key, value) -> {
            try {
                Site site = new Site(value);
                if (site.isChat()) {
                    CHAT_SITES.put(key, site);
                }
                if (site.isTts()) {
                    TTS_SITES.put(key, site);
                }
            } catch (Exception e) {
                TouhouLittleMaid.LOGGER.error("Failed to load site: {}", key, e);
            }
        });

        // 保存
        saveSites();
    }

    public static void saveSites() {
        Map<String, LinkedHashMap<String, Object>> allSites = Maps.newLinkedHashMap();
        CHAT_SITES.forEach((key, value) -> allSites.put(key, value.siteToMap()));
        TTS_SITES.forEach((key, value) -> allSites.put(key, value.siteToMap()));

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        // 保存
        try (FileWriter writer = new FileWriter(SITES_FILES.toFile(), StandardCharsets.UTF_8)) {
            yaml.dump(allSites, writer);
        } catch (Exception e) {
            TouhouLittleMaid.LOGGER.error("Failed to save available sites file", e);
        }
    }

    public static Site getChatSite(String key) {
        return CHAT_SITES.get(key);
    }

    public static Site getTtsSite(String key) {
        return TTS_SITES.get(key);
    }

    @Nullable
    public static Site getFirstAvailableChatSite() {
        return CHAT_SITES.values().stream().filter(site -> StringUtils.isNotBlank(site.getApiKey())).findFirst().orElse(null);
    }

    public static Site getFirstAvailableTtsSite() {
        return TTS_SITES.values().stream().filter(site -> StringUtils.isNotBlank(site.getApiKey())).findFirst().orElse(null);
    }
}

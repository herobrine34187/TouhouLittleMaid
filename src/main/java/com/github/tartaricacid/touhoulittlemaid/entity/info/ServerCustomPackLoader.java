package com.github.tartaricacid.touhoulittlemaid.entity.info;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.ChairModelInfo;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.CustomModelPack;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatText;
import com.github.tartaricacid.touhoulittlemaid.entity.info.models.ServerChairModels;
import com.github.tartaricacid.touhoulittlemaid.entity.info.models.ServerMaidModels;
import com.github.tartaricacid.touhoulittlemaid.util.ZipFileCheck;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.LOGGER;

public final class ServerCustomPackLoader {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(ChatText.class, new ChatText.Serializer())
            .create();
    public static final ServerMaidModels SERVER_MAID_MODELS = ServerMaidModels.getInstance();
    public static final ServerChairModels SERVER_CHAIR_MODELS = ServerChairModels.getInstance();
    private static final Map<Long, Path> CRC32_FILE_MAP = Maps.newHashMap();
    private static final String CUSTOM_PACK_DIR_NAME = "tlm_custom_pack";
    private static final Path PACK_FOLDER = Paths.get(CUSTOM_PACK_DIR_NAME);
    private static final Marker MARKER = MarkerManager.getMarker("ServerCustomPackLoader");
    private static final Pattern DOMAIN = Pattern.compile("^assets/([\\w.]+)/$");

    public static void reloadPacks() {
        SettingReader.clear();
        SERVER_MAID_MODELS.clearAll();
        SERVER_CHAIR_MODELS.clearAll();
        CRC32_FILE_MAP.clear();
        initPacks();
        SettingReader.reloadSettings();
    }

    private static void initPacks() {
        File packFolder = PACK_FOLDER.toFile();
        if (!packFolder.isDirectory()) {
            try {
                Files.createDirectories(packFolder.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        loadPacks(packFolder);
    }

    private static void loadPacks(File packFolder) {
        File[] files = packFolder.listFiles(((dir, name) -> true));
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                try {
                    if (ZipFileCheck.isZipFile(file)) {
                        readModelFromZipFile(file);
                    } else {
                        TouhouLittleMaid.LOGGER.error("{} file is corrupt and cannot be loaded.", file.getName());
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (file.isDirectory()) {
                readModelFromFolder(file);
            }
        }
    }

    public static void readModelFromFolder(File root) {
        File[] domainFiles = root.toPath().resolve("assets").toFile().listFiles((dir, name) -> true);
        if (domainFiles == null) {
            return;
        }
        for (File domainDir : domainFiles) {
            if (domainDir.isDirectory()) {
                Path rootPath = root.toPath();
                String domain = domainDir.getName();
                loadMaidModelPack(rootPath, domain);
                // 读取 AI 预设
                SettingReader.readCustomPack(rootPath, domain);
                loadChairModelPack(rootPath, domain);
            }
        }
    }

    public static void readModelFromZipFile(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> iteration = zipFile.entries();
            while (iteration.hasMoreElements()) {
                Matcher matcher = DOMAIN.matcher(iteration.nextElement().getName());
                if (matcher.find()) {
                    String domain = matcher.group(1);
                    loadMaidModelPack(zipFile, domain);
                    // 读取 AI 预设
                    SettingReader.readCustomPack(zipFile, domain);
                    loadChairModelPack(zipFile, domain);
                    // 文件夹形式的不记录 crc32，也不往客户端同步
                    loadCrc32Info(file);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void loadCrc32Info(File file) throws IOException {
        long crc32 = FileUtils.checksumCRC32(file);
        CRC32_FILE_MAP.putIfAbsent(crc32, file.toPath());
    }

    private static void loadMaidModelPack(Path rootPath, String domain) {
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loading...");
        File file = rootPath.resolve("assets").resolve(domain).resolve(SERVER_MAID_MODELS.getJsonFileName()).toFile();
        if (!file.isFile()) {
            return;
        }
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            CustomModelPack<MaidModelInfo> pack = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<CustomModelPack<MaidModelInfo>>() {
                    }.getType());
            pack.decorate(domain);
            for (MaidModelInfo maidModelInfo : pack.getModelList()) {
                if (maidModelInfo.getEasterEgg() == null) {
                    String id = maidModelInfo.getModelId().toString();
                    SERVER_MAID_MODELS.putInfo(id, maidModelInfo);
                    LOGGER.debug(MARKER, "Loaded model info: {}", id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            LOGGER.warn(MARKER, "Fail to parse model pack in domain {}", domain);
            e.printStackTrace();
        }
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loaded");
    }

    private static void loadMaidModelPack(ZipFile zipFile, String domain) {
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loading...");
        ZipEntry entry = zipFile.getEntry(String.format("assets/%s/%s", domain, SERVER_MAID_MODELS.getJsonFileName()));
        if (entry == null) {
            return;
        }
        try (InputStream stream = zipFile.getInputStream(entry)) {
            CustomModelPack<MaidModelInfo> pack = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<CustomModelPack<MaidModelInfo>>() {
                    }.getType());
            // 加载时有非常小的概率会偶发，不知为何
            if (pack == null) {
                LOGGER.warn(MARKER, "Model pack in domain {} is null, file is {}", domain, zipFile.getName());
                return;
            }
            pack.decorate(domain);
            for (MaidModelInfo maidModelInfo : pack.getModelList()) {
                if (maidModelInfo.getEasterEgg() == null) {
                    String id = maidModelInfo.getModelId().toString();
                    SERVER_MAID_MODELS.putInfo(id, maidModelInfo);
                    LOGGER.debug(MARKER, "Loaded model info: {}", id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            LOGGER.warn(MARKER, "Fail to parse model pack in domain {}", domain);
            e.printStackTrace();
        }
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loaded");
    }

    private static void loadChairModelPack(ZipFile zipFile, String domain) {
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loading...");
        ZipEntry entry = zipFile.getEntry(String.format("assets/%s/%s", domain, SERVER_CHAIR_MODELS.getJsonFileName()));
        if (entry == null) {
            return;
        }
        try (InputStream stream = zipFile.getInputStream(entry)) {
            CustomModelPack<ChairModelInfo> pack = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<CustomModelPack<ChairModelInfo>>() {
                    }.getType());
            // 加载时有非常小的概率会偶发，不知为何
            if (pack == null) {
                LOGGER.warn(MARKER, "Model pack in domain {} is null, file is {}", domain, zipFile.getName());
                return;
            }
            pack.decorate(domain);
            for (ChairModelInfo chairModelInfo : pack.getModelList()) {
                String id = chairModelInfo.getModelId().toString();
                SERVER_CHAIR_MODELS.putInfo(id, chairModelInfo);
                LOGGER.debug(MARKER, "Loaded model info: {}", id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            LOGGER.warn(MARKER, "Fail to parse model pack in domain {}", domain);
            e.printStackTrace();
        }
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loaded");
    }

    private static void loadChairModelPack(Path rootPath, String domain) {
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loading...");
        File file = rootPath.resolve("assets").resolve(domain).resolve(SERVER_CHAIR_MODELS.getJsonFileName()).toFile();
        if (!file.isFile()) {
            return;
        }
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            CustomModelPack<ChairModelInfo> pack = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<CustomModelPack<ChairModelInfo>>() {
                    }.getType());
            pack.decorate(domain);
            for (ChairModelInfo chairModelInfo : pack.getModelList()) {
                String id = chairModelInfo.getModelId().toString();
                SERVER_CHAIR_MODELS.putInfo(id, chairModelInfo);
                LOGGER.debug(MARKER, "Loaded model info: {}", id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            LOGGER.warn(MARKER, "Fail to parse model pack in domain {}", domain);
            e.printStackTrace();
        }
        LOGGER.debug(MARKER, "Touhou little maid mod's model is loaded");
    }

    public static Map<Long, Path> getCrc32FileMap() {
        return CRC32_FILE_MAP;
    }
}

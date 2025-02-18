package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterSetting {
    private static final String COMMENTS = "#";
    private final String rawSetting;

    public CharacterSetting(File settingFile) throws IOException {
        String text = FileUtils.readFileToString(settingFile, StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder();
        processText(text).forEach(value -> builder.append(value).append("\n"));
        this.rawSetting = builder.toString();
    }

    public CharacterSetting(InputStream stream) throws IOException {
        String text = IOUtils.toString(stream, StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder();
        processText(text).forEach(value -> builder.append(value).append("\n"));
        this.rawSetting = builder.toString();
    }

    private static List<String> processText(String text) {
        return Arrays.stream(StringUtils.split(text, "\n"))
                .map(StringUtils::trim)
                .filter(s -> !s.startsWith(COMMENTS))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    public String getSetting(EntityMaid maid, String language) {
        return PapiReplacer.replace(rawSetting, maid, language);
    }
}
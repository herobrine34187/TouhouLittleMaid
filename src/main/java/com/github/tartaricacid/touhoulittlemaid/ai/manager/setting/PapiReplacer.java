package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Service;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TODO: 有必要支持多语言版本么？现在的大语言模型都自带翻译功能？
public class PapiReplacer {
    static String replace(String input, EntityMaid maid, String language) {
        Level level = maid.level;
        return input.replace("${game_time}", getTime(level))
                .replace("${weather}", getWeather(level))
                .replace("${dimension}", getDimension(level))
                .replace("${mainhand_item}", getSlotItemName(EquipmentSlot.MAINHAND, maid))
                .replace("${offhand_item}", getSlotItemName(EquipmentSlot.OFFHAND, maid))
                .replace("${inventory_items}", getInventoryItems(maid))
                .replace("${output_json_format}", getOutputJsonFormat())
                .replace("${chat_language}", language)
                .replace("${tts_language}", ttsLanguage(maid.getAiChatManager().getTtsLanguage()));
    }

    /**
     * 不能调用 LanguageManager，那个是客户端方法
     */
    private static String ttsLanguage(String languageTag) {
        // 将语言代码转换为 Locale 所需的格式，例如 zh_cn -> zh-CN
        String[] parts = languageTag.split("_");
        if (parts.length == 2) {
            languageTag = parts[0] + "-" + parts[1].toUpperCase(Locale.ENGLISH);
        }
        Locale locale = Locale.forLanguageTag(languageTag);
        return locale.getDisplayLanguage() + " (" + locale.getDisplayCountry() + ")";
    }

    private static String getWeather(Level level) {
        if (level.isThundering()) {
            return "雷雨天";
        }
        if (level.isRaining()) {
            return "阴雨天";
        }
        return "晴天";
    }

    private static String getTime(Level level) {
        long time = level.getDayTime();
        long hours = (time / 1000 + 6) % 24;
        long minutes = (time % 1000) / (50 / 3);
        return String.format("%02d:%02d", hours, minutes);
    }

    private static String getDimension(Level level) {
        ResourceKey<Level> dimension = level.dimension();
        if (dimension == Level.OVERWORLD) {
            return "主世界";
        }
        if (dimension == Level.NETHER) {
            return "下界";
        }
        if (dimension == Level.END) {
            return "末地";
        }
        return dimension.location().toString();
    }

    private static String getSlotItemName(EquipmentSlot slot, EntityMaid maid) {
        ItemStack stack = maid.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return "空的";
        }
        String itemName = stack.getDisplayName().getString();
        int count = stack.getCount();
        return String.format("%sx%s", itemName, count);
    }

    private static String getInventoryItems(EntityMaid maid) {
        List<String> names = new ArrayList<>();
        RangedWrapper backpack = maid.getAvailableBackpackInv();
        for (int i = 0; i < backpack.getSlots(); i++) {
            ItemStack stack = backpack.getStackInSlot(i);
            if (!stack.isEmpty()) {
                String itemName = stack.getDisplayName().getString();
                int count = stack.getCount();
                names.add(String.format("%sx%s", itemName, count));
            }
        }
        if (names.isEmpty()) {
            return "空的";
        }
        return StringUtils.join(names, ", ");
    }

    private static String getOutputJsonFormat() {
        return Service.GSON.toJson(new ResponseChat());
    }
}

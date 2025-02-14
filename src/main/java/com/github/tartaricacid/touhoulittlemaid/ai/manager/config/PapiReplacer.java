package com.github.tartaricacid.touhoulittlemaid.ai.manager.config;

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

// FIXME: 支持多语言版本
public class PapiReplacer {
    static String replace(String input, EntityMaid maid) {
        Level level = maid.level;
        return input.replace("${game_time}", getTime(level))
                .replace("${weather}", getWeather(level))
                .replace("${dimension}", getDimension(level))
                .replace("${mainhand_item}", getSlotItemName(EquipmentSlot.MAINHAND, maid))
                .replace("${offhand_item}", getSlotItemName(EquipmentSlot.OFFHAND, maid))
                .replace("${inventory_items}", getInventoryItems(maid))
                .replace("${output_json_format}", getOutputJsonFormat())
                .replace("${chat_language}", AIConfig.CHAT_LANGUAGE.get())
                .replace("${tts_language}", AIConfig.TTS_LANGUAGE.get());
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
        long hours = time / 1000 + 6;
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

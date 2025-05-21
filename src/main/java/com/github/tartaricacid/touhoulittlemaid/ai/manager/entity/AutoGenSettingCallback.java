package com.github.tartaricacid.touhoulittlemaid.ai.manager.entity;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ErrorCode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.StringUtils;

public class AutoGenSettingCallback extends LLMCallback {
    public AutoGenSettingCallback(MaidAIChatManager chatManager, String message) {
        super(chatManager, message);
    }

    @Override
    public void onSuccess(ResponseChat responseChat) {
        String result = responseChat.getChatText();
        if (StringUtils.isBlank(result)) {
            onFailure(null, new Throwable("Error in Response Chat: %s".formatted(responseChat)), ErrorCode.CHAT_TEXT_IS_EMPTY);
            return;
        }
        chatManager.customSetting = result.replaceAll("\n+", "\n\n");
        LivingEntity owner = maid.getOwner();
        if (owner instanceof Player player) {
            player.sendSystemMessage(Component.translatable("ai.touhou_little_maid.chat.llm.auto_gen_setting").withStyle(ChatFormatting.GRAY));
        }
    }
}

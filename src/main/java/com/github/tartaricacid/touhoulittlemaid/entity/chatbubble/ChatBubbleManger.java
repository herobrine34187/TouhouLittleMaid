package com.github.tartaricacid.touhoulittlemaid.entity.chatbubble;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;


/**
 * 旧版的聊天气泡管理器，因为新的设计和实现方式不一样，所以这个类就弃用了
 * <p>
 * 注意这个类的名字也是拼写错误：Man_ger -> Man_a_ger
 */
@Deprecated
public class ChatBubbleManger {
    @Deprecated
    public static void addInnerChatText(EntityMaid maid, String key) {
        maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.type2(Component.translatable(key)));
    }

    /**
     * 可以直接异步加载
     */
    @Deprecated
    public static void addAiChatTextSync(EntityMaid maid, String message) {
        if (StringUtils.isNotBlank(message) && maid.level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            server.submit(() -> addAiChatText(maid, message));
        }
    }

    @Deprecated
    public static void addAiChatText(EntityMaid maid, String message) {
        Component component = Component.literal(message);
        maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.type2(component));

        // 给主人发送聊天栏信息
        if (maid.getOwner() instanceof ServerPlayer player) {
            Component name = maid.getName();
            MutableComponent msg = Component.literal("<").append(name).append(">").append(CommonComponents.SPACE).append(message);
            player.sendSystemMessage(msg.withStyle(ChatFormatting.GRAY));
        }
    }
}
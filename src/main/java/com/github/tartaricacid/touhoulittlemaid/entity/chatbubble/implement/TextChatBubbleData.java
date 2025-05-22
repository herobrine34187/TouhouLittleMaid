package com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.IChatBubbleRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.implement.TextChatBubbleRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TextChatBubbleData implements IChatBubbleData {
    public static final int DEFAULT_EXIST_TICK = 15 * 20;
    public static final ResourceLocation ID = new ResourceLocation(TouhouLittleMaid.MOD_ID, "text");
    public static final ResourceLocation TYPE_1 = new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/chat_bubble/type1.png");
    public static final ResourceLocation TYPE_2 = new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/chat_bubble/type2.png");

    private final int existTick;
    private final ResourceLocation bg;
    private final int priority;
    private Component text;

    private TextChatBubbleData(int existTick, Component text, ResourceLocation bg, int priority) {
        this.existTick = existTick;
        this.text = text;
        this.bg = bg;
        this.priority = priority;
    }

    private TextChatBubbleData(int existTick, Component text, ResourceLocation bg) {
        this(existTick, text, bg, 0);
    }

    public static TextChatBubbleData type1(Component text) {
        return new TextChatBubbleData(DEFAULT_EXIST_TICK, text, TYPE_1);
    }

    public static TextChatBubbleData type2(Component text) {
        return new TextChatBubbleData(DEFAULT_EXIST_TICK, text, TYPE_2);
    }

    public static TextChatBubbleData create(int existTick, Component text, ResourceLocation bg, int priority) {
        return new TextChatBubbleData(existTick, text, bg, priority);
    }

    @Override
    public int existTick() {
        return this.existTick;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    public void setText(Component text) {
        this.text = text;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IChatBubbleRenderer getRenderer(IChatBubbleRenderer.Position position) {
        return new TextChatBubbleRenderer(this.text, this.bg, position);
    }

    public static class TextChatSerializer implements IChatBubbleData.ChatSerializer {
        @Override
        public IChatBubbleData readFromBuff(FriendlyByteBuf buf) {
            return new TextChatBubbleData(buf.readVarInt(), buf.readComponent(), buf.readResourceLocation());
        }

        @Override
        public void writeToBuff(FriendlyByteBuf buf, IChatBubbleData data) {
            TextChatBubbleData textChat = (TextChatBubbleData) data;
            buf.writeVarInt(textChat.existTick);
            buf.writeComponent(textChat.text);
            buf.writeResourceLocation(textChat.bg);
        }
    }
}

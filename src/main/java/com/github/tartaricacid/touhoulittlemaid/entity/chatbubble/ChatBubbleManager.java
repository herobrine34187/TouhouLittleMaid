package com.github.tartaricacid.touhoulittlemaid.entity.chatbubble;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import javax.annotation.Nullable;

import static com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid.getChatBubbleKey;

public class ChatBubbleManager {
    private final EntityMaid maid;

    public ChatBubbleManager(EntityMaid maid) {
        this.maid = maid;
    }

    public void tick() {
        // 每 5 tick 检查一次
        if (this.maid.tickCount % 5 != 0) {
            return;
        }
        boolean update = this.getChatBubbleDataCollection().update();
        if (update) {
            this.forceUpdateChatBubble();
        }
    }

    public ChatBubbleDataCollection getChatBubbleDataCollection() {
        return maid.getEntityData().get(getChatBubbleKey());
    }

    @Nullable
    public IChatBubbleData getChatBubble(long key) {
        return this.getChatBubbleDataCollection().get(key);
    }

    public void removeChatBubble(long key) {
        this.getChatBubbleDataCollection().remove(key);
        this.forceUpdateChatBubble();
    }

    /**
     * 返回存入的 key
     *
     * @param bubble 聊天气泡
     * @return 如果存入失败则返回 -1
     */
    public long addChatBubble(IChatBubbleData bubble) {
        long key = this.getChatBubbleDataCollection().add(bubble);
        this.forceUpdateChatBubble();
        return key;
    }

    public void forceUpdateChatBubble() {
        maid.getEntityData().set(getChatBubbleKey(), this.getChatBubbleDataCollection(), true);
    }
}

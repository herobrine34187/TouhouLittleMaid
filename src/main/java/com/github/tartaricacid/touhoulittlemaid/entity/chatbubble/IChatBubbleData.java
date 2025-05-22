package com.github.tartaricacid.touhoulittlemaid.entity.chatbubble;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.IChatBubbleRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IChatBubbleData {
    /**
     * 气泡的持续时间
     *
     * @return 单位：tick
     */
    int existTick();

    /**
     * 气泡对应的序列化器的注册 ID
     *
     * @return 气泡对应的序列化器的注册 ID
     */
    ResourceLocation id();

    /**
     * 气泡的优先级，数字越大越高
     *
     * @return 气泡的优先级
     */
    default int priority() {
        return 0;
    }

    /**
     * 客户端的渲染类
     *
     * @param position 排列位置
     * @return 渲染类
     */
    @OnlyIn(Dist.CLIENT)
    IChatBubbleRenderer getRenderer(IChatBubbleRenderer.Position position);

    /**
     * 数据的序列化器，用于服务端向客户端同步数据
     */
    interface ChatSerializer {
        /**
         * 读取数据
         *
         * @param buf 数据包
         * @return 数据
         */
        IChatBubbleData readFromBuff(FriendlyByteBuf buf);

        /**
         * 写入数据
         *
         * @param buf  数据包
         * @param data 数据
         */
        void writeToBuff(FriendlyByteBuf buf, IChatBubbleData data);
    }
}

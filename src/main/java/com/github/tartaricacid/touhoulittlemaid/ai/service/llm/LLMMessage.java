package com.github.tartaricacid.touhoulittlemaid.ai.service.llm;


import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 大语言模型消息
 *
 * @param role     消息角色
 * @param message  消息内容
 * @param gameTime 游戏时间，目前暂时没有用途
 */
public record LLMMessage(Role role, String message, long gameTime) {
    public static Codec<LLMMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Role.CODEC.fieldOf("role").forGetter(LLMMessage::role),
            Codec.STRING.fieldOf("message").forGetter(LLMMessage::message),
            Codec.LONG.fieldOf("game_time").forGetter(LLMMessage::gameTime)
    ).apply(instance, LLMMessage::new));

    public static LLMMessage userChat(EntityMaid maid, String message) {
        long time = maid.level.getGameTime();
        return new LLMMessage(Role.USER, message, time);
    }

    public static LLMMessage assistantChat(EntityMaid maid, String message) {
        long time = maid.level.getGameTime();
        return new LLMMessage(Role.ASSISTANT, message, time);
    }

    public static LLMMessage systemChat(EntityMaid maid, String message) {
        long time = maid.level.getGameTime();
        return new LLMMessage(Role.SYSTEM, message, time);
    }
}
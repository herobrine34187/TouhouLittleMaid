package com.github.tartaricacid.touhoulittlemaid.ai.service.llm;


import com.github.tartaricacid.touhoulittlemaid.ai.service.Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ResponseCallback;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import java.util.List;

public interface LLMClient extends Client {
    /**
     * 大语言模型聊天接口
     *
     * @param maid     交谈的女仆
     * @param messages 聊天的上下文，包括提示词，历史记录，用户输入内容
     * @param config   聊天配置
     * @param callback 回调，返回聊天结果字符串
     */
    void chat(EntityMaid maid, List<LLMMessage> messages, LLMConfig config, ResponseCallback<String> callback);
}

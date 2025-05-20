package com.github.tartaricacid.touhoulittlemaid.ai.service.function.response;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;

/**
 * 当不需要返回工具调用结果时，返回的对象
 *
 * @param responseChat 需要给 TTS 和玩家返回的内容
 */
public record EndToolResponse(ResponseChat responseChat) implements ToolResponse {
}

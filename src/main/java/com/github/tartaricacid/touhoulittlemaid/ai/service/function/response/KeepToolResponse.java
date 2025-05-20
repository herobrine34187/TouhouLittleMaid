package com.github.tartaricacid.touhoulittlemaid.ai.service.function.response;

/**
 * 当需要给 AI 返回工具调用结果时，返回的对象
 *
 * @param message 工具调用结果
 */
public record KeepToolResponse(String message) implements ToolResponse {
}

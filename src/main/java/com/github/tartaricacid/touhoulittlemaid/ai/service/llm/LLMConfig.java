package com.github.tartaricacid.touhoulittlemaid.ai.service.llm;

/**
 * 大语言模型配置类
 * 用于存储大语言模型的配置参数
 *
 * @param model       模型名称
 * @param temperature 温度，控制生成文本的随机性
 * @param maxTokens   最大生成的 token 数量
 */
public record LLMConfig(String model, double temperature, int maxTokens) {
}

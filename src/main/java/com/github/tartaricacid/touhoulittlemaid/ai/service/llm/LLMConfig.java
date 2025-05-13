package com.github.tartaricacid.touhoulittlemaid.ai.service.llm;

import com.github.tartaricacid.touhoulittlemaid.ai.service.Model;

public record LLMConfig(Model model, double temperature, int maxTokens) {
}

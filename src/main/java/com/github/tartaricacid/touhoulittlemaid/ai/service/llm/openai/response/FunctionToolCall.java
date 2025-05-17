package com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response;

import com.google.gson.annotations.SerializedName;

public class FunctionToolCall {
    @SerializedName("name")
    private String name;

    @SerializedName("arguments")
    private String arguments;

    public String getName() {
        return name;
    }

    public String getArguments() {
        return arguments;
    }
}

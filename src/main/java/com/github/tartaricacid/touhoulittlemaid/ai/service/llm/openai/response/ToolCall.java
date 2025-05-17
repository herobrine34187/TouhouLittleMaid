package com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response;

import com.google.gson.annotations.SerializedName;

public class ToolCall {
    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("function")
    private FunctionToolCall functionToolCall;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public FunctionToolCall getFunction() {
        return functionToolCall;
    }
}

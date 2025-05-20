package com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response;

import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FunctionToolCall {
    public static Codec<FunctionToolCall> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(FunctionToolCall::getName),
            Codec.STRING.fieldOf("arguments").forGetter(FunctionToolCall::getArguments)
    ).apply(instance, FunctionToolCall::new));

    @SerializedName("name")
    private String name;

    @SerializedName("arguments")
    private String arguments;

    public FunctionToolCall(String name, String arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public String getArguments() {
        return arguments;
    }
}

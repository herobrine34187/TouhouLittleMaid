package com.github.tartaricacid.touhoulittlemaid.ai.service.stt;

public enum STTApiType {
    PLAYER2("player2"),
    ALIYUN("aliyun"),
    SILICONFLOW("siliconflow");

    private final String name;

    STTApiType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

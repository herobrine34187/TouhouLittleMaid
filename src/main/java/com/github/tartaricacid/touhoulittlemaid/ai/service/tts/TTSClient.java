package com.github.tartaricacid.touhoulittlemaid.ai.service.tts;


import com.github.tartaricacid.touhoulittlemaid.ai.service.Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ResponseCallback;

public interface TTSClient extends Client {
    /**
     * 语音合成接口
     *
     * @param message  需要合成的文本
     * @param config   语音合成配置
     * @param callback 回调，返回合成的音频数据
     */
    void play(String message, TTSConfig config, ResponseCallback<byte[]> callback);
}

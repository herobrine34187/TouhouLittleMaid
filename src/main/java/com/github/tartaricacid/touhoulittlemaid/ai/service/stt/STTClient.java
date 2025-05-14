package com.github.tartaricacid.touhoulittlemaid.ai.service.stt;

import com.github.tartaricacid.touhoulittlemaid.ai.service.Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ResponseCallback;

/**
 * 语音识别客户端接口
 * 因为不同的语音识别服务，返回结果的时机不一致，故分别对起始和结束都传递了回调
 */
public interface STTClient extends Client {
    /**
     * 开始语音识别，在按下语音识别键时调用
     *
     * @param config   语音识别配置，目前为空
     * @param callback 回调，返回识别结果字符串
     */
    void startRecord(STTConfig config, ResponseCallback<String> callback);

    /**
     * 停止语音识别，在松开语音识别键时调用
     *
     * @param config   语音识别配置，目前为空
     * @param callback 回调，返回识别结果字符串
     */
    void stopRecord(STTConfig config, ResponseCallback<String> callback);
}

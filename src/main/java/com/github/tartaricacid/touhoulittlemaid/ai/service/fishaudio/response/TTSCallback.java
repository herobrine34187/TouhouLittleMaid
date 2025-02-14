package com.github.tartaricacid.touhoulittlemaid.ai.service.fishaudio.response;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ResponseCallback;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class TTSCallback implements ResponseCallback<byte[]> {
    private final Consumer<byte[]> consumer;

    public TTSCallback(Consumer<byte[]> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onFailure(HttpRequest request, Throwable e) {
        TouhouLittleMaid.LOGGER.error("Request failed: {}", request, e);
    }

    @Override
    public void onResponse(HttpResponse<byte[]> response) {
        if (isSuccessful(response)) {
            consumer.accept(response.body());
        } else {
            TouhouLittleMaid.LOGGER.error("Request failed: {}", response.statusCode());
        }
    }
}

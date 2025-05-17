package com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai;


import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ResponseCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.FunctionCallRegister;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.FunctionTool;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMMessage;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.Role;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ResponseFormat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.Tool;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response.ChatCompletionResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response.FunctionToolCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response.Message;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public final class LLMOpenAIClient implements LLMClient {
    private final HttpClient httpClient;
    private final LLMOpenAISite site;

    public LLMOpenAIClient(HttpClient httpClient, LLMOpenAISite site) {
        this.httpClient = httpClient;
        this.site = site;
    }

    @Override
    public void chat(EntityMaid maid, List<LLMMessage> messages, LLMConfig config, ResponseCallback<String> callback) {
        URI url = URI.create(this.site.url());
        String apiKey = this.site.secretKey();
        String model = config.model();
        double temperature = config.temperature();
        int maxTokens = config.maxTokens();

        // 构建对话
        ChatCompletion chatCompletion = ChatCompletion.create().model(model).maxTokens(maxTokens)
                .temperature(temperature).setResponseFormat(ResponseFormat.json());
        // 添加消息
        for (LLMMessage message : messages) {
            if (message.role() == Role.USER) {
                chatCompletion.userChat(message.message());
            } else if (message.role() == Role.ASSISTANT) {
                chatCompletion.assistantChat(message.message());
            } else if (message.role() == Role.SYSTEM) {
                chatCompletion.systemChat(message.message());
            }
        }
        // 添加 function call tool
        if (AIConfig.FUNCTION_CALL_ENABLED.get()) {
            this.addFunctionCalls(chatCompletion);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(chatCompletion)))
                .timeout(Duration.ofSeconds(20))
                .uri(url);

        this.site.headers().forEach(builder::header);
        HttpRequest httpRequest = builder.build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) ->
                        handle(maid, callback, response, throwable, httpRequest));
    }

    private void addFunctionCalls(ChatCompletion chatCompletion) {
        FunctionCallRegister.getFunctionCalls().forEach((key, value) -> {
            String id = value.getId();
            String description = value.getDescription();
            ObjectParameter root = ObjectParameter.create();
            Parameter parameter = value.addParameters(root);
            Tool tool = FunctionTool.create()
                    .setName(id)
                    .setDescription(description)
                    .setParameters(parameter).build();
            chatCompletion.addTool(tool);
        });
    }

    private void handle(EntityMaid maid, ResponseCallback<String> callback, HttpResponse<String> response, Throwable throwable, HttpRequest httpRequest) {
        if (throwable != null) {
            callback.onFailure(httpRequest, throwable);
            return;
        }
        try {
            String string = response.body();
            if (isSuccessful(response)) {
                ChatCompletionResponse completionResponse = GSON.fromJson(string, ChatCompletionResponse.class);
                Message firstChoice = completionResponse.getFirstChoice();
                if (firstChoice != null) {
                    this.onSuccess(maid, callback, firstChoice);
                    return;
                }
                String message = String.format("No Choice Found: %s", response);
                callback.onFailure(httpRequest, new Throwable(message));
                TouhouLittleMaid.LOGGER.error(message);
            } else {
                TouhouLittleMaid.LOGGER.error("Request failed: {}", string);
                String message = String.format("HTTP Error Code: %d, Response %s", response.statusCode(), string);
                callback.onFailure(httpRequest, new Throwable(message));
            }
        } catch (JsonSyntaxException e) {
            TouhouLittleMaid.LOGGER.error("JSON Syntax Exception: ", e);
            callback.onFailure(httpRequest, e);
        }
    }

    @SuppressWarnings("all")
    private void onSuccess(EntityMaid maid, ResponseCallback<String> callback, Message firstChoice) {
        if (!firstChoice.hasToolCall()) {
            callback.onSuccess(firstChoice.getContent());
            return;
        }
        firstChoice.getToolCalls().forEach(toolCall -> {
            FunctionToolCall function = toolCall.getFunction();
            String name = function.getName();
            String arguments = function.getArguments();
            IFunctionCall call = FunctionCallRegister.getFunctionCall(name);
            if (call == null) {
                return;
            }
            try {
                JsonObject parse = GsonHelper.parse(arguments);
                call.codec().parse(JsonOps.INSTANCE, parse)
                        .resultOrPartial(TouhouLittleMaid.LOGGER::error)
                        .ifPresent(result -> onAsyncCallFunction(maid, result, call, arguments));
            } catch (Exception e) {
                TouhouLittleMaid.LOGGER.error("Error while parsing JSON: {}", e.getMessage());
            }
        });
    }

    @SuppressWarnings("all")
    private void onAsyncCallFunction(EntityMaid maid, Object result, IFunctionCall call, String arguments) {
        // 因为获取网络流是在独立的线程上，所以需要推送到主线程执行
        if (maid.level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().submit(() -> {
                call.onToolCall(result, maid);
                // 需要记录下工具调用，方便 debug
                TouhouLittleMaid.LOGGER.debug("Function call: {}, arguments is {}", call.getId(), arguments);
            });
        }
    }
}

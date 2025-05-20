package com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai;


import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.ErrorCode;
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
import io.github.haibiiin.json.repair.JSONRepair;
import io.github.haibiiin.json.repair.JSONRepairConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public final class LLMOpenAIClient implements LLMClient {
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final LLMOpenAISite site;

    public LLMOpenAIClient(HttpClient httpClient, LLMOpenAISite site) {
        this.httpClient = httpClient;
        this.site = site;
    }

    @Override
    public void chat(EntityMaid maid, List<LLMMessage> messages, LLMConfig config, ResponseCallback<ResponseChat> callback) {
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
            this.addFunctionCalls(maid, chatCompletion);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(chatCompletion)))
                .timeout(MAX_TIMEOUT).uri(url);
        this.site.headers().forEach(builder::header);
        HttpRequest httpRequest = builder.build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) ->
                        handle(maid, callback, response, throwable, httpRequest));
    }

    private void addFunctionCalls(EntityMaid maid, ChatCompletion chatCompletion) {
        FunctionCallRegister.getFunctionCalls().forEach((key, value) -> {
            String id = value.getId();
            String description = value.getDescription(maid);
            ObjectParameter root = ObjectParameter.create();
            Parameter parameter = value.addParameters(root, maid);
            Tool tool = FunctionTool.create().setName(id)
                    .setDescription(description)
                    .setParameters(parameter).build();
            chatCompletion.addTool(tool);
        });
    }

    private void handle(EntityMaid maid, ResponseCallback<ResponseChat> callback, HttpResponse<String> response, Throwable throwable, HttpRequest request) {
        this.<ChatCompletionResponse>handleResponse(callback, response, throwable, request, chat -> {
            Message firstChoice = chat.getFirstChoice();
            if (firstChoice == null) {
                String message = "No Choice Found: %s".formatted(response);
                callback.onFailure(request, new Throwable(message), ErrorCode.CHAT_CHOICE_IS_EMPTY);
                return;
            }
            if (firstChoice.hasToolCall()) {
                this.onFunctionCall(maid, callback, request, firstChoice);
            } else {
                this.onTextCall(callback, request, firstChoice);
            }
        }, ChatCompletionResponse.class);
    }

    private void onTextCall(ResponseCallback<ResponseChat> callback, HttpRequest request, Message firstChoice) {
        String content = firstChoice.getContent();
        ResponseChat chat;
        try {
            // 修正 JSON
            JSONRepairConfig config = new JSONRepairConfig();
            config.enableExtractJSON();
            JSONRepair repair = new JSONRepair(config);
            String correct = repair.handle(content);
            chat = Client.GSON.fromJson(correct, ResponseChat.class);
            callback.onSuccess(chat);
        } catch (JsonSyntaxException error) {
            String message = "Exception %s, JSON is: %s".formatted(error.getLocalizedMessage(), content);
            callback.onFailure(request, new Throwable(message), ErrorCode.JSON_DECODE_ERROR);
        }
    }

    @SuppressWarnings("all")
    private void onFunctionCall(EntityMaid maid, ResponseCallback<ResponseChat> callback, HttpRequest request, Message firstChoice) {
        firstChoice.getToolCalls().forEach(toolCall -> {
            FunctionToolCall function = toolCall.getFunction();
            String name = function.getName();
            String arguments = function.getArguments();
            IFunctionCall functionCall = FunctionCallRegister.getFunctionCall(name);
            if (functionCall == null) {
                return;
            }
            try {
                JsonObject parse = GsonHelper.parse(arguments);
                functionCall.codec().parse(JsonOps.INSTANCE, parse)
                        .resultOrPartial(TouhouLittleMaid.LOGGER::error)
                        .ifPresent(result -> onAsyncFunctionCall(maid, result, functionCall, arguments, callback));
            } catch (JsonSyntaxException exception) {
                String message = "Exception %s, JSON is: %s".formatted(exception.getLocalizedMessage(), arguments);
                callback.onFailure(request, new Throwable(message), ErrorCode.JSON_DECODE_ERROR);
            }
        });
    }

    @SuppressWarnings("all")
    private void onAsyncFunctionCall(EntityMaid maid, Object result, IFunctionCall functionCall,
                                     String arguments, ResponseCallback<ResponseChat> callback) {
        // 因为获取网络流是在独立的线程上，所以需要推送到主线程执行
        if (maid.level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().submit(() -> {
                ResponseChat responseChat = functionCall.onToolCall(result, maid);
                if (responseChat != null) {
                    callback.onSuccess(responseChat);
                }
                // 需要记录下工具调用，方便 debug
                TouhouLittleMaid.LOGGER.debug("Use function call: {}, arguments is {}", functionCall.getId(), arguments);
            });
        }
    }
}

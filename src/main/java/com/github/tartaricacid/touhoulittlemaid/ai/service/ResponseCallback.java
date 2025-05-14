package com.github.tartaricacid.touhoulittlemaid.ai.service;

import java.net.http.HttpRequest;

/**
 * 响应回调接口
 * 用于处理异步请求的响应
 *
 * @param <T> 响应数据类型
 */
public interface ResponseCallback<T> {
    /**
     * 请求失败时调用
     *
     * @param request 请求对象
     * @param e       异常信息
     */
    void onFailure(HttpRequest request, Throwable e);

    /**
     * 请求成功时调用
     *
     * @param response 响应数据
     */
    void onSuccess(T response);
}

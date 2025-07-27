/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.application.multimodal.core;

import com.alibaba.cloud.ai.application.multimodal.model.MultiModalRequest;
import com.alibaba.cloud.ai.application.multimodal.model.MultiModalResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 多模态处理器核心接口
 * 定义统一的处理规范，支持同步和异步处理
 * 
 * @param <T> 处理器特定的请求类型
 * @param <R> 处理器特定的响应类型
 */
public interface MultiModalProcessor<T extends MultiModalRequest, R extends MultiModalResponse> {

    /**
     * 获取处理器支持的模态类型
     * @return 模态类型枚举
     */
    ModalityType getSupportedModality();

    /**
     * 检查是否支持指定的请求类型
     * @param request 多模态请求
     * @return 是否支持
     */
    boolean supports(MultiModalRequest request);

    /**
     * 同步处理多模态请求
     * @param request 多模态请求
     * @return 处理结果
     */
    Mono<R> process(T request);

    /**
     * 流式处理多模态请求
     * @param request 多模态请求
     * @return 流式处理结果
     */
    Flux<R> processStream(T request);

    /**
     * 批量处理多模态请求
     * @param requests 多模态请求列表
     * @return 批量处理结果
     */
    default Flux<R> processBatch(Flux<T> requests) {
        return requests.flatMap(this::process);
    }

    /**
     * 处理器优先级，数值越小优先级越高
     * @return 优先级
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 预处理请求，用于参数验证和标准化
     * @param request 原始请求
     * @return 预处理后的请求
     */
    default T preProcess(T request) {
        return request;
    }

    /**
     * 后处理响应，用于结果格式化和增强
     * @param response 原始响应
     * @param request 对应的请求
     * @return 后处理后的响应
     */
    default R postProcess(R response, T request) {
        return response;
    }
}
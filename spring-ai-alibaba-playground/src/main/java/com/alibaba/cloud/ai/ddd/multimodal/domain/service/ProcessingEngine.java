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

package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 处理引擎领域服务接口
 * 定义多模态内容处理的核心能力
 */
public interface ProcessingEngine {

    /**
     * 检查是否支持指定的处理任务
     *
     * @param task 处理任务
     * @return 是否支持
     */
    boolean supports(ProcessingTask task);

    /**
     * 同步处理任务
     *
     * @param task 处理任务
     * @return 处理结果
     */
    Mono<ProcessingResult> process(ProcessingTask task);

    /**
     * 流式处理任务
     *
     * @param task 处理任务
     * @return 流式处理结果
     */
    Flux<ProcessingResult> processStream(ProcessingTask task);

    /**
     * 获取引擎优先级
     * 数值越小优先级越高
     *
     * @return 优先级
     */
    int getPriority();

    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    String getEngineName();

    /**
     * 预估处理时间（毫秒）
     *
     * @param task 处理任务
     * @return 预估时间
     */
    long estimateProcessingTime(ProcessingTask task);

    /**
     * 检查引擎健康状态
     *
     * @return 是否健康
     */
    boolean isHealthy();
}
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
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 任务调度器领域服务接口
 * 负责处理任务的调度和分发
 */
public interface TaskScheduler {

    /**
     * 调度单个任务
     *
     * @param task 处理任务
     * @return 调度结果
     */
    Mono<Void> scheduleTask(ProcessingTask task);

    /**
     * 批量调度任务
     *
     * @param tasks 任务列表
     * @return 调度结果
     */
    Mono<Void> scheduleTasks(List<ProcessingTask> tasks);

    /**
     * 取消任务调度
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    Mono<Boolean> cancelTask(ProcessingTaskId taskId);

    /**
     * 获取待处理任务队列长度
     *
     * @return 队列长度
     */
    int getPendingTasksCount();

    /**
     * 获取正在处理的任务数量
     *
     * @return 处理中任务数量
     */
    int getProcessingTasksCount();

    /**
     * 检查调度器是否健康
     *
     * @return 是否健康
     */
    boolean isHealthy();

    /**
     * 获取调度器负载状态
     *
     * @return 负载百分比 (0-100)
     */
    int getLoadPercentage();
}
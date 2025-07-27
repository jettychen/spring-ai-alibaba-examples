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

package com.alibaba.cloud.ai.ddd.multimodal.domain.repository;

import com.alibaba.cloud.ai.ddd.shared.domain.Repository;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingStatus;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ModalityType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 处理任务仓储接口
 * 定义处理任务的持久化操作
 */
public interface ProcessingTaskRepository extends Repository<ProcessingTask, ProcessingTaskId> {

    /**
     * 根据用户ID查找任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<ProcessingTask> findByUserId(String userId);

    /**
     * 根据状态查找任务
     *
     * @param status 处理状态
     * @return 任务列表
     */
    List<ProcessingTask> findByStatus(ProcessingStatus status);

    /**
     * 根据模态类型查找任务
     *
     * @param inputModality 输入模态
     * @param outputModality 输出模态
     * @return 任务列表
     */
    List<ProcessingTask> findByModalityTypes(ModalityType inputModality, ModalityType outputModality);

    /**
     * 查找指定时间范围内的任务
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务列表
     */
    List<ProcessingTask> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据优先级查找待处理任务
     *
     * @param maxPriority 最大优先级
     * @param limit 限制数量
     * @return 任务列表
     */
    List<ProcessingTask> findPendingTasksByPriority(int maxPriority, int limit);

    /**
     * 查找超时的处理中任务
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时任务列表
     */
    List<ProcessingTask> findTimeoutProcessingTasks(int timeoutMinutes);

    /**
     * 根据用户ID和状态查找任务
     *
     * @param userId 用户ID
     * @param status 处理状态
     * @return 任务列表
     */
    List<ProcessingTask> findByUserIdAndStatus(String userId, ProcessingStatus status);

    /**
     * 统计指定状态的任务数量
     *
     * @param status 处理状态
     * @return 任务数量
     */
    long countByStatus(ProcessingStatus status);

    /**
     * 统计指定用户的任务数量
     *
     * @param userId 用户ID
     * @return 任务数量
     */
    long countByUserId(String userId);

    /**
     * 删除指定时间之前的已完成任务
     *
     * @param beforeTime 时间点
     * @return 删除的任务数量
     */
    long deleteCompletedTasksBefore(LocalDateTime beforeTime);
}
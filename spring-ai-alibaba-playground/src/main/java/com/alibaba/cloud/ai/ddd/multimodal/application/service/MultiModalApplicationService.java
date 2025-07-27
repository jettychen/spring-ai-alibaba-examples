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

package com.alibaba.cloud.ai.ddd.multimodal.application.service;

import com.alibaba.cloud.ai.ddd.multimodal.application.command.CreateProcessingTaskCommand;
import com.alibaba.cloud.ai.ddd.multimodal.application.command.ProcessTaskCommand;
import com.alibaba.cloud.ai.ddd.multimodal.application.handler.CreateProcessingTaskHandler;
import com.alibaba.cloud.ai.ddd.multimodal.application.handler.ProcessTaskHandler;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingResult;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingStatus;
import com.alibaba.cloud.ai.ddd.multimodal.domain.repository.ProcessingTaskRepository;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.ProcessingOrchestrator;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * 多模态应用服务
 * 协调多模态处理的各种用例，是应用层的入口
 */
@Service
@Transactional
public class MultiModalApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(MultiModalApplicationService.class);

    private final CreateProcessingTaskHandler createTaskHandler;
    private final ProcessTaskHandler processTaskHandler;
    private final ProcessingTaskRepository taskRepository;
    private final ProcessingOrchestrator orchestrator;

    public MultiModalApplicationService(CreateProcessingTaskHandler createTaskHandler,
                                       ProcessTaskHandler processTaskHandler,
                                       ProcessingTaskRepository taskRepository,
                                       ProcessingOrchestrator orchestrator) {
        this.createTaskHandler = createTaskHandler;
        this.processTaskHandler = processTaskHandler;
        this.taskRepository = taskRepository;
        this.orchestrator = orchestrator;
    }

    /**
     * 创建并立即处理任务
     *
     * @param command 创建任务命令
     * @return 处理结果
     */
    public Mono<ProcessingResult> createAndProcessTask(CreateProcessingTaskCommand command) {
        logger.info("Creating and processing task for user: {}", command.getUserId());

        return createTaskHandler.handle(command)
                .flatMap(taskId -> {
                    ProcessTaskCommand processCommand = new ProcessTaskCommand(taskId, command.isStreaming());
                    return processTaskHandler.handle(processCommand);
                })
                .onErrorMap(throwable -> {
                    logger.error("Failed to create and process task", throwable);
                    if (throwable instanceof DomainException) {
                        return throwable;
                    }
                    return new DomainException("PROCESSING_FAILED", 
                        "Failed to process task: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * 创建并流式处理任务
     *
     * @param command 创建任务命令
     * @return 处理结果流
     */
    public Flux<ProcessingResult> createAndProcessTaskStream(CreateProcessingTaskCommand command) {
        logger.info("Creating and streaming processing task for user: {}", command.getUserId());

        return createTaskHandler.handle(command)
                .flatMapMany(taskId -> {
                    ProcessTaskCommand processCommand = new ProcessTaskCommand(taskId, true);
                    return processTaskHandler.handleStream(processCommand);
                })
                .onErrorResume(throwable -> {
                    logger.error("Failed to create and stream process task", throwable);
                    if (throwable instanceof DomainException) {
                        return Flux.error(throwable);
                    }
                    return Flux.error(new DomainException("STREAMING_FAILED", 
                        "Failed to stream process task: " + throwable.getMessage(), throwable));
                });
    }

    /**
     * 仅创建任务（不立即处理）
     *
     * @param command 创建任务命令
     * @return 任务ID
     */
    public Mono<String> createTask(CreateProcessingTaskCommand command) {
        logger.info("Creating task for user: {}", command.getUserId());
        return createTaskHandler.handle(command);
    }

    /**
     * 处理已存在的任务
     *
     * @param taskId 任务ID
     * @param streaming 是否流式处理
     * @return 处理结果
     */
    public Mono<ProcessingResult> processTask(String taskId, boolean streaming) {
        logger.info("Processing existing task: {}", taskId);
        ProcessTaskCommand command = new ProcessTaskCommand(taskId, streaming);
        return processTaskHandler.handle(command);
    }

    /**
     * 流式处理已存在的任务
     *
     * @param taskId 任务ID
     * @return 处理结果流
     */
    public Flux<ProcessingResult> processTaskStream(String taskId) {
        logger.info("Streaming processing existing task: {}", taskId);
        ProcessTaskCommand command = new ProcessTaskCommand(taskId, true);
        return processTaskHandler.handleStream(command);
    }

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Transactional(readOnly = true)
    public Mono<ProcessingTask> getTask(String taskId) {
        return Mono.fromCallable(() -> {
            ProcessingTaskId id = ProcessingTaskId.of(taskId);
            return taskRepository.findById(id)
                    .orElseThrow(() -> new DomainException("TASK_NOT_FOUND", 
                        "Task not found: " + taskId));
        });
    }

    /**
     * 获取用户的任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    public Mono<List<ProcessingTask>> getUserTasks(String userId) {
        return Mono.fromCallable(() -> taskRepository.findByUserId(userId));
    }

    /**
     * 获取用户指定状态的任务列表
     *
     * @param userId 用户ID
     * @param status 任务状态
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    public Mono<List<ProcessingTask>> getUserTasksByStatus(String userId, ProcessingStatus status) {
        return Mono.fromCallable(() -> taskRepository.findByUserIdAndStatus(userId, status));
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    public Mono<Void> cancelTask(String taskId) {
        return Mono.fromCallable(() -> {
            ProcessingTaskId id = ProcessingTaskId.of(taskId);
            Optional<ProcessingTask> optionalTask = taskRepository.findById(id);
            
            if (optionalTask.isEmpty()) {
                throw new DomainException("TASK_NOT_FOUND", "Task not found: " + taskId);
            }
            
            ProcessingTask task = optionalTask.get();
            task.cancel();
            taskRepository.save(task);
            
            logger.info("Cancelled task: {}", taskId);
            return null;
        });
    }

    /**
     * 重试失败的任务
     *
     * @param taskId 任务ID
     * @return 处理结果
     */
    public Mono<ProcessingResult> retryTask(String taskId) {
        return Mono.fromCallable(() -> {
            ProcessingTaskId id = ProcessingTaskId.of(taskId);
            Optional<ProcessingTask> optionalTask = taskRepository.findById(id);
            
            if (optionalTask.isEmpty()) {
                throw new DomainException("TASK_NOT_FOUND", "Task not found: " + taskId);
            }
            
            ProcessingTask task = optionalTask.get();
            task.retry();
            taskRepository.save(task);
            
            logger.info("Retrying task: {}", taskId);
            return task;
        })
        .flatMap(task -> {
            ProcessTaskCommand command = new ProcessTaskCommand(taskId, false);
            return processTaskHandler.handle(command);
        });
    }

    /**
     * 获取系统状态
     *
     * @return 系统状态信息
     */
    @Transactional(readOnly = true)
    public Mono<SystemStatus> getSystemStatus() {
        return Mono.fromCallable(() -> {
            long pendingCount = taskRepository.countByStatus(ProcessingStatus.PENDING);
            long processingCount = taskRepository.countByStatus(ProcessingStatus.PROCESSING);
            long completedCount = taskRepository.countByStatus(ProcessingStatus.COMPLETED);
            long failedCount = taskRepository.countByStatus(ProcessingStatus.FAILED);
            
            boolean systemHealthy = orchestrator.isSystemHealthy();
            int availableEngines = orchestrator.getAvailableEngineCount();
            
            return new SystemStatus(pendingCount, processingCount, completedCount, 
                                  failedCount, systemHealthy, availableEngines);
        });
    }

    /**
     * 系统状态记录
     */
    public record SystemStatus(
        long pendingTasks,
        long processingTasks,
        long completedTasks,
        long failedTasks,
        boolean systemHealthy,
        int availableEngines
    ) {}
}
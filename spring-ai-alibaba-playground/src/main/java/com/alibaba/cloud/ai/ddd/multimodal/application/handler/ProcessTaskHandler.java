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

package com.alibaba.cloud.ai.ddd.multimodal.application.handler;

import com.alibaba.cloud.ai.ddd.multimodal.application.command.ProcessTaskCommand;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingResult;
import com.alibaba.cloud.ai.ddd.multimodal.domain.repository.ProcessingTaskRepository;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.ProcessingEngine;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.ProcessingOrchestrator;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 处理任务处理器
 * 执行多模态处理任务的业务逻辑
 */
@Component
public class ProcessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskHandler.class);
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(10);

    private final ProcessingTaskRepository taskRepository;
    private final ProcessingOrchestrator orchestrator;

    public ProcessTaskHandler(ProcessingTaskRepository taskRepository, 
                             ProcessingOrchestrator orchestrator) {
        this.taskRepository = taskRepository;
        this.orchestrator = orchestrator;
    }

    /**
     * 处理任务（同步）
     *
     * @param command 处理命令
     * @return 处理结果
     */
    public Mono<ProcessingResult> handle(ProcessTaskCommand command) {
        return Mono.fromCallable(() -> ProcessingTaskId.of(command.getTaskId()))
                .flatMap(taskId -> 
                    Mono.fromCallable(() -> taskRepository.findById(taskId))
                        .flatMap(optionalTask -> {
                            if (optionalTask.isEmpty()) {
                                return Mono.error(new DomainException("TASK_NOT_FOUND", 
                                    "Processing task not found: " + taskId));
                            }
                            return Mono.just(optionalTask.get());
                        })
                )
                .flatMap(this::processTask)
                .timeout(PROCESSING_TIMEOUT)
                .onErrorResume(throwable -> {
                    logger.error("Failed to process task: {}", command.getTaskId(), throwable);
                    return handleProcessingError(command.getTaskId(), throwable);
                });
    }

    /**
     * 处理任务（流式）
     *
     * @param command 处理命令
     * @return 处理结果流
     */
    public Flux<ProcessingResult> handleStream(ProcessTaskCommand command) {
        return Mono.fromCallable(() -> ProcessingTaskId.of(command.getTaskId()))
                .flatMapMany(taskId -> 
                    Mono.fromCallable(() -> taskRepository.findById(taskId))
                        .flatMapMany(optionalTask -> {
                            if (optionalTask.isEmpty()) {
                                return Flux.error(new DomainException("TASK_NOT_FOUND", 
                                    "Processing task not found: " + taskId));
                            }
                            return processTaskStream(optionalTask.get());
                        })
                )
                .timeout(PROCESSING_TIMEOUT)
                .onErrorResume(throwable -> {
                    logger.error("Failed to process task stream: {}", command.getTaskId(), throwable);
                    return handleProcessingErrorStream(command.getTaskId(), throwable);
                });
    }

    /**
     * 处理单个任务
     */
    private Mono<ProcessingResult> processTask(ProcessingTask task) {
        logger.info("Processing task: {}", task.getId());

        return Mono.fromCallable(() -> {
            // 开始处理
            task.startProcessing();
            taskRepository.save(task);
            return task;
        })
        .flatMap(orchestrator::processTask)
        .map(result -> {
            // 完成处理
            task.completeProcessing(result, 0); // 处理时间将在orchestrator中计算
            taskRepository.save(task);
            
            logger.info("Successfully processed task: {}", task.getId());
            return result;
        })
        .onErrorResume(error -> {
            // 处理失败
            task.markAsFailed(error.getMessage());
            taskRepository.save(task);
            return Mono.error(error);
        });
    }

    /**
     * 流式处理任务
     */
    private Flux<ProcessingResult> processTaskStream(ProcessingTask task) {
        logger.info("Processing task stream: {}", task.getId());

        return Mono.fromCallable(() -> {
            // 1. 选择处理引擎
            ProcessingEngine engine = orchestrator.selectEngine(task)
                .orElseThrow(() -> new DomainException("NO_SUITABLE_ENGINE", 
                    "No suitable processing engine found for task: " + task.getId()));

            // 2. 开始处理
            task.startProcessing();
            taskRepository.save(task);

            return engine;
        })
        .flatMapMany(engine -> {
            long startTime = System.currentTimeMillis();
            
            return engine.processStream(task)
                .doOnComplete(() -> {
                    // 处理完成，但没有最终结果，创建一个空结果
                    long processingTime = System.currentTimeMillis() - startTime;
                    ProcessingResult emptyResult = ProcessingResult.textResult("", 1.0);
                    task.completeProcessing(emptyResult, processingTime);
                    taskRepository.save(task);
                    
                    logger.info("Successfully processed task stream: {} in {}ms", 
                               task.getId(), processingTime);
                })
                .doOnError(error -> {
                    // 处理失败
                    task.markAsFailed(error.getMessage());
                    taskRepository.save(task);
                });
        });
    }

    /**
     * 处理错误
     */
    private Mono<ProcessingResult> handleProcessingError(String taskId, Throwable throwable) {
        return Mono.fromCallable(() -> {
            ProcessingTaskId id = ProcessingTaskId.of(taskId);
            return taskRepository.findById(id)
                .map(task -> {
                    task.markAsFailed(throwable.getMessage());
                    taskRepository.save(task);
                    return task;
                })
                .orElse(null);
        })
        .then(Mono.error(throwable));
    }

    /**
     * 处理流式错误
     */
    private Flux<ProcessingResult> handleProcessingErrorStream(String taskId, Throwable throwable) {
        return Mono.fromCallable(() -> {
            ProcessingTaskId id = ProcessingTaskId.of(taskId);
            return taskRepository.findById(id)
                .map(task -> {
                    task.markAsFailed(throwable.getMessage());
                    taskRepository.save(task);
                    return task;
                })
                .orElse(null);
        })
        .then(Mono.error(throwable))
        .flatMapMany(unused -> Flux.error(throwable));
    }
}
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

package com.alibaba.cloud.ai.ddd.multimodal.domain.model;

import com.alibaba.cloud.ai.ddd.shared.domain.Entity;
import com.alibaba.cloud.ai.ddd.multimodal.domain.event.ProcessingTaskCreated;
import com.alibaba.cloud.ai.ddd.multimodal.domain.event.ProcessingTaskCompleted;
import com.alibaba.cloud.ai.ddd.multimodal.domain.event.ProcessingTaskFailed;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 处理任务聚合根
 * 表示一个完整的多模态处理任务，包含输入、输出和处理状态
 */
public class ProcessingTask extends Entity<ProcessingTaskId> {

    private final ProcessingTaskId id;
    private final String userId;
    private final ModalityType inputModality;
    private final ModalityType outputModality;
    private final ProcessingPrompt prompt;
    private final List<InputContent> inputContents;
    private final Map<String, String> parameters;
    private final LocalDateTime createdAt;
    private final int priority;
    
    private ProcessingStatus status;
    private ProcessingResult result;
    private String errorMessage;
    private LocalDateTime completedAt;
    private long processingTimeMs;

    private ProcessingTask(ProcessingTaskId id, String userId, ModalityType inputModality, 
                          ModalityType outputModality, ProcessingPrompt prompt, 
                          List<InputContent> inputContents, Map<String, String> parameters, int priority) {
        this.id = Objects.requireNonNull(id, "Task ID cannot be null");
        this.userId = userId;
        this.inputModality = Objects.requireNonNull(inputModality, "Input modality cannot be null");
        this.outputModality = Objects.requireNonNull(outputModality, "Output modality cannot be null");
        this.prompt = Objects.requireNonNull(prompt, "Prompt cannot be null");
        this.inputContents = List.copyOf(inputContents != null ? inputContents : List.of());
        this.parameters = Map.copyOf(parameters != null ? parameters : Map.of());
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.status = ProcessingStatus.PENDING;
        
        validateBusinessRules();
        addDomainEvent(new ProcessingTaskCreated(id, inputModality, outputModality));
    }

    /**
     * 创建新的处理任务
     *
     * @param id 任务标识
     * @param userId 用户标识
     * @param inputModality 输入模态
     * @param outputModality 输出模态
     * @param prompt 处理提示
     * @param inputContents 输入内容列表
     * @param parameters 处理参数
     * @param priority 优先级
     * @return 处理任务
     */
    public static ProcessingTask create(ProcessingTaskId id, String userId, 
                                       ModalityType inputModality, ModalityType outputModality,
                                       ProcessingPrompt prompt, List<InputContent> inputContents,
                                       Map<String, String> parameters, int priority) {
        return new ProcessingTask(id, userId, inputModality, outputModality, prompt, 
                                inputContents, parameters, priority);
    }

    /**
     * 开始处理任务
     */
    public void startProcessing() {
        if (status != ProcessingStatus.PENDING) {
            throw new DomainException("INVALID_STATUS", 
                "Cannot start processing task in status: " + status);
        }
        this.status = ProcessingStatus.PROCESSING;
    }

    /**
     * 完成任务处理
     *
     * @param result 处理结果
     * @param processingTimeMs 处理时间（毫秒）
     */
    public void completeProcessing(ProcessingResult result, long processingTimeMs) {
        if (status != ProcessingStatus.PROCESSING) {
            throw new DomainException("INVALID_STATUS", 
                "Cannot complete task in status: " + status);
        }
        
        this.result = Objects.requireNonNull(result, "Processing result cannot be null");
        this.processingTimeMs = processingTimeMs;
        this.completedAt = LocalDateTime.now();
        this.status = ProcessingStatus.COMPLETED;
        
        addDomainEvent(new ProcessingTaskCompleted(id, result));
    }

    /**
     * 标记任务失败
     *
     * @param errorMessage 错误消息
     */
    public void markAsFailed(String errorMessage) {
        if (status == ProcessingStatus.COMPLETED) {
            throw new DomainException("INVALID_STATUS", 
                "Cannot mark completed task as failed");
        }
        
        this.errorMessage = Objects.requireNonNull(errorMessage, "Error message cannot be null");
        this.completedAt = LocalDateTime.now();
        this.status = ProcessingStatus.FAILED;
        
        addDomainEvent(new ProcessingTaskFailed(id, errorMessage));
    }

    /**
     * 重试任务
     */
    public void retry() {
        if (status != ProcessingStatus.FAILED) {
            throw new DomainException("INVALID_STATUS", 
                "Can only retry failed tasks");
        }
        
        this.status = ProcessingStatus.PENDING;
        this.errorMessage = null;
        this.completedAt = null;
        this.processingTimeMs = 0;
    }

    /**
     * 取消任务
     */
    public void cancel() {
        if (status == ProcessingStatus.COMPLETED) {
            throw new DomainException("INVALID_STATUS", 
                "Cannot cancel completed task");
        }
        
        this.status = ProcessingStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 检查任务是否已完成
     */
    public boolean isCompleted() {
        return status == ProcessingStatus.COMPLETED;
    }

    /**
     * 检查任务是否失败
     */
    public boolean isFailed() {
        return status == ProcessingStatus.FAILED;
    }

    /**
     * 检查任务是否正在处理
     */
    public boolean isProcessing() {
        return status == ProcessingStatus.PROCESSING;
    }

    /**
     * 检查是否需要输入文件
     */
    public boolean requiresInputFiles() {
        return inputModality != ModalityType.TEXT;
    }

    /**
     * 获取指定参数值
     */
    public String getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * 获取指定参数值，如果不存在则使用默认值
     */
    public String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    private void validateBusinessRules() {
        // 验证模态类型兼容性
        if (inputModality == ModalityType.TEXT && inputContents.isEmpty() && prompt.isEmpty()) {
            throw new DomainException("INVALID_INPUT", 
                "Text input modality requires either input content or non-empty prompt");
        }
        
        if (inputModality != ModalityType.TEXT && inputContents.isEmpty()) {
            throw new DomainException("INVALID_INPUT", 
                "Non-text input modality requires input content");
        }
        
        if (!inputModality.isInputSupported()) {
            throw new DomainException("UNSUPPORTED_MODALITY", 
                "Input modality not supported: " + inputModality.getCode());
        }
        
        if (!outputModality.isOutputSupported()) {
            throw new DomainException("UNSUPPORTED_MODALITY", 
                "Output modality not supported: " + outputModality.getCode());
        }
    }

    // Getters
    @Override
    public ProcessingTaskId getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public ModalityType getInputModality() {
        return inputModality;
    }

    public ModalityType getOutputModality() {
        return outputModality;
    }

    public ProcessingPrompt getPrompt() {
        return prompt;
    }

    public List<InputContent> getInputContents() {
        return inputContents;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getPriority() {
        return priority;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public ProcessingResult getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
}
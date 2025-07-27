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

package com.alibaba.cloud.ai.ddd.multimodal.interfaces.dto;

import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 处理任务响应DTO
 * 用于向前端返回处理任务信息
 */
public class ProcessingTaskResponse {

    private String taskId;
    private String userId;
    private String inputModality;
    private String outputModality;
    private String prompt;
    private String status;
    private String content;
    private byte[] binaryContent;
    private String contentType;
    private double confidence;
    private Map<String, Object> metadata;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private long processingTimeMs;

    public ProcessingTaskResponse() {}

    /**
     * 从处理任务创建响应
     */
    public static ProcessingTaskResponse fromTask(ProcessingTask task) {
        ProcessingTaskResponse response = new ProcessingTaskResponse();
        response.taskId = task.getId().getValue();
        response.userId = task.getUserId();
        response.inputModality = task.getInputModality().getCode();
        response.outputModality = task.getOutputModality().getCode();
        response.prompt = task.getPrompt().getContent();
        response.status = task.getStatus().getCode();
        response.errorMessage = task.getErrorMessage();
        response.createdAt = task.getCreatedAt();
        response.completedAt = task.getCompletedAt();
        response.processingTimeMs = task.getProcessingTimeMs();

        // 如果有处理结果，填充结果信息
        if (task.getResult() != null) {
            ProcessingResult result = task.getResult();
            response.content = result.getContent();
            response.binaryContent = result.getBinaryContent();
            response.contentType = result.getContentType();
            response.confidence = result.getConfidence();
            response.metadata = result.getMetadata();
        }

        return response;
    }

    /**
     * 从处理结果创建响应
     */
    public static ProcessingTaskResponse fromResult(String taskId, ProcessingResult result) {
        ProcessingTaskResponse response = new ProcessingTaskResponse();
        response.taskId = taskId;
        response.status = "completed";
        response.content = result.getContent();
        response.binaryContent = result.getBinaryContent();
        response.contentType = result.getContentType();
        response.confidence = result.getConfidence();
        response.metadata = result.getMetadata();
        response.completedAt = result.getGeneratedAt();
        return response;
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInputModality() {
        return inputModality;
    }

    public void setInputModality(String inputModality) {
        this.inputModality = inputModality;
    }

    public String getOutputModality() {
        return outputModality;
    }

    public void setOutputModality(String outputModality) {
        this.outputModality = outputModality;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getBinaryContent() {
        return binaryContent;
    }

    public void setBinaryContent(byte[] binaryContent) {
        this.binaryContent = binaryContent;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public boolean hasTextContent() {
        return content != null && !content.trim().isEmpty();
    }

    public boolean hasBinaryContent() {
        return binaryContent != null && binaryContent.length > 0;
    }
}
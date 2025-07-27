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

package com.alibaba.cloud.ai.application.multimodal.model;

import com.alibaba.cloud.ai.application.multimodal.core.ModalityType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 多模态响应基础模型
 * 包含所有模态处理结果的通用字段和方法
 */
public class MultiModalResponse {
    
    private String responseId;
    private String requestId;
    private ModalityType outputModality;
    private String content;
    private byte[] binaryContent;
    private String contentType;
    private Map<String, Object> metadata;
    private ProcessingStatus status;
    private String errorMessage;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
    private Double confidence;

    public MultiModalResponse() {
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
        this.status = ProcessingStatus.SUCCESS;
    }

    public MultiModalResponse(String requestId, ModalityType outputModality) {
        this();
        this.requestId = requestId;
        this.outputModality = outputModality;
    }

    // Getters and Setters
    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ModalityType getOutputModality() {
        return outputModality;
    }

    public void setOutputModality(ModalityType outputModality) {
        this.outputModality = outputModality;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessage != null) {
            this.status = ProcessingStatus.ERROR;
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    // Utility methods
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public <T> T getMetadata(String key, Class<T> type) {
        Object value = this.metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public boolean isSuccess() {
        return status == ProcessingStatus.SUCCESS;
    }

    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    public boolean hasBinaryContent() {
        return binaryContent != null && binaryContent.length > 0;
    }

    public static MultiModalResponse success(String requestId, ModalityType outputModality, String content) {
        MultiModalResponse response = new MultiModalResponse(requestId, outputModality);
        response.setContent(content);
        response.setStatus(ProcessingStatus.SUCCESS);
        return response;
    }

    public static MultiModalResponse success(String requestId, ModalityType outputModality, byte[] binaryContent, String contentType) {
        MultiModalResponse response = new MultiModalResponse(requestId, outputModality);
        response.setBinaryContent(binaryContent);
        response.setContentType(contentType);
        response.setStatus(ProcessingStatus.SUCCESS);
        return response;
    }

    public static MultiModalResponse error(String requestId, String errorMessage) {
        MultiModalResponse response = new MultiModalResponse();
        response.setRequestId(requestId);
        response.setErrorMessage(errorMessage);
        response.setStatus(ProcessingStatus.ERROR);
        return response;
    }

    @Override
    public String toString() {
        return "MultiModalResponse{" +
                "responseId='" + responseId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", outputModality=" + outputModality +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", binaryContentLength=" + (binaryContent != null ? binaryContent.length : 0) +
                ", status=" + status +
                ", processingTimeMs=" + processingTimeMs +
                ", confidence=" + confidence +
                '}';
    }

    /**
     * 处理状态枚举
     */
    public enum ProcessingStatus {
        SUCCESS("success", "处理成功"),
        PROCESSING("processing", "处理中"),
        ERROR("error", "处理失败"),
        TIMEOUT("timeout", "处理超时"),
        CANCELLED("cancelled", "处理取消");

        private final String code;
        private final String description;

        ProcessingStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
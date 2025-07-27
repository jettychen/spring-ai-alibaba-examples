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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 多模态请求基础模型
 * 包含所有模态处理的通用字段和方法
 */
public class MultiModalRequest {
    
    private String requestId;
    private String userId;
    private ModalityType inputModality;
    private ModalityType outputModality;
    private String prompt;
    private List<MultipartFile> files;
    private Map<String, Object> parameters;
    private Map<String, String> metadata;
    private LocalDateTime timestamp;
    private Integer priority;
    private boolean streaming;

    public MultiModalRequest() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.parameters = new HashMap<>();
        this.metadata = new HashMap<>();
        this.priority = 0;
        this.streaming = false;
    }

    public MultiModalRequest(String prompt, ModalityType inputModality, ModalityType outputModality) {
        this();
        this.prompt = prompt;
        this.inputModality = inputModality;
        this.outputModality = outputModality;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ModalityType getInputModality() {
        return inputModality;
    }

    public void setInputModality(ModalityType inputModality) {
        this.inputModality = inputModality;
    }

    public ModalityType getOutputModality() {
        return outputModality;
    }

    public void setOutputModality(ModalityType outputModality) {
        this.outputModality = outputModality;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    // Utility methods
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public <T> T getParameter(String key, Class<T> type) {
        Object value = this.parameters.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.get(key);
    }

    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    public MultipartFile getFirstFile() {
        return hasFiles() ? files.get(0) : null;
    }

    @Override
    public String toString() {
        return "MultiModalRequest{" +
                "requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", inputModality=" + inputModality +
                ", outputModality=" + outputModality +
                ", prompt='" + prompt + '\'' +
                ", filesCount=" + (files != null ? files.size() : 0) +
                ", timestamp=" + timestamp +
                ", priority=" + priority +
                ", streaming=" + streaming +
                '}';
    }
}
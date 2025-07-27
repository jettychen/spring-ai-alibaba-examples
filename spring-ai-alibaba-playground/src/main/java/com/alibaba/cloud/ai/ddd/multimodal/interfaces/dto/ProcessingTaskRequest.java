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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Map;

/**
 * 处理任务请求DTO
 * 用于接收前端的处理任务创建请求
 */
public class ProcessingTaskRequest {

    @NotBlank(message = "Input modality cannot be blank")
    private String inputModality;

    @NotBlank(message = "Output modality cannot be blank")
    private String outputModality;

    private String prompt;

    private Map<String, String> parameters;

    @Min(value = 0, message = "Priority must be at least 0")
    @Max(value = 10, message = "Priority must be at most 10")
    private int priority = 5;

    private boolean streaming = false;

    public ProcessingTaskRequest() {}

    public ProcessingTaskRequest(String inputModality, String outputModality, String prompt) {
        this.inputModality = inputModality;
        this.outputModality = outputModality;
        this.prompt = prompt;
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    @Override
    public String toString() {
        return String.format("ProcessingTaskRequest{inputModality='%s', outputModality='%s', " +
                           "promptLength=%d, priority=%d, streaming=%s}",
                           inputModality, outputModality, 
                           prompt != null ? prompt.length() : 0, priority, streaming);
    }
}
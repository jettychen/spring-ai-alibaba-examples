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

package com.alibaba.cloud.ai.ddd.multimodal.application.command;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 创建处理任务命令
 * 封装创建新处理任务所需的所有参数
 */
public class CreateProcessingTaskCommand {

    private final String userId;
    private final String inputModalityCode;
    private final String outputModalityCode;
    private final String prompt;
    private final List<InputFileData> inputFiles;
    private final Map<String, String> parameters;
    private final int priority;
    private final boolean streaming;

    public CreateProcessingTaskCommand(String userId, String inputModalityCode, String outputModalityCode,
                                     String prompt, List<InputFileData> inputFiles, 
                                     Map<String, String> parameters, int priority, boolean streaming) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.inputModalityCode = Objects.requireNonNull(inputModalityCode, "Input modality code cannot be null");
        this.outputModalityCode = Objects.requireNonNull(outputModalityCode, "Output modality code cannot be null");
        this.prompt = prompt != null ? prompt : "";
        this.inputFiles = inputFiles != null ? List.copyOf(inputFiles) : List.of();
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.priority = priority;
        this.streaming = streaming;
    }

    public String getUserId() {
        return userId;
    }

    public String getInputModalityCode() {
        return inputModalityCode;
    }

    public String getOutputModalityCode() {
        return outputModalityCode;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<InputFileData> getInputFiles() {
        return inputFiles;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isStreaming() {
        return streaming;
    }

    /**
     * 输入文件数据
     */
    public static class InputFileData {
        private final String fileName;
        private final byte[] content;
        private final String contentType;

        public InputFileData(String fileName, byte[] content, String contentType) {
            this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
            this.content = Objects.requireNonNull(content, "Content cannot be null").clone();
            this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content.clone();
        }

        public String getContentType() {
            return contentType;
        }
    }

    @Override
    public String toString() {
        return String.format("CreateProcessingTaskCommand{userId='%s', inputModality='%s', outputModality='%s', " +
                           "promptLength=%d, filesCount=%d, priority=%d, streaming=%s}",
                           userId, inputModalityCode, outputModalityCode, 
                           prompt.length(), inputFiles.size(), priority, streaming);
    }
}
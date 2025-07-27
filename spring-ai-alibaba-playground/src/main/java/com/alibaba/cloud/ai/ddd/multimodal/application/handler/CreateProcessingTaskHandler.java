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

import com.alibaba.cloud.ai.ddd.multimodal.application.command.CreateProcessingTaskCommand;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.*;
import com.alibaba.cloud.ai.ddd.multimodal.domain.repository.ProcessingTaskRepository;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 创建处理任务处理器
 * 处理创建新的多模态处理任务的业务逻辑
 */
@Component
public class CreateProcessingTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateProcessingTaskHandler.class);

    private final ProcessingTaskRepository taskRepository;

    public CreateProcessingTaskHandler(ProcessingTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * 处理创建任务命令
     *
     * @param command 创建任务命令
     * @return 创建的任务ID
     */
    public Mono<String> handle(CreateProcessingTaskCommand command) {
        return Mono.fromCallable(() -> {
            logger.info("Creating processing task: {}", command);

            try {
                // 1. 验证命令参数
                validateCommand(command);

                // 2. 转换模态类型
                ModalityType inputModality = ModalityType.fromCode(command.getInputModalityCode());
                ModalityType outputModality = ModalityType.fromCode(command.getOutputModalityCode());

                // 3. 创建提示词
                ProcessingPrompt prompt = command.getPrompt().isEmpty() ? 
                    ProcessingPrompt.defaultFor(inputModality) : 
                    ProcessingPrompt.of(command.getPrompt());

                // 4. 转换输入内容
                List<InputContent> inputContents = command.getInputFiles().stream()
                    .map(fileData -> InputContent.of(
                        fileData.getFileName(),
                        fileData.getContent(),
                        fileData.getContentType()
                    ))
                    .collect(Collectors.toList());

                // 5. 创建处理任务
                ProcessingTaskId taskId = ProcessingTaskId.generate();
                ProcessingTask task = ProcessingTask.create(
                    taskId,
                    command.getUserId(),
                    inputModality,
                    outputModality,
                    prompt,
                    inputContents,
                    command.getParameters(),
                    command.getPriority()
                );

                // 6. 保存任务
                ProcessingTask savedTask = taskRepository.save(task);
                
                logger.info("Successfully created processing task: {}", savedTask.getId());
                return savedTask.getId().getValue();

            } catch (Exception e) {
                logger.error("Failed to create processing task: {}", command, e);
                throw new DomainException("TASK_CREATION_FAILED", 
                    "Failed to create processing task: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 验证命令参数
     */
    private void validateCommand(CreateProcessingTaskCommand command) {
        // 验证模态类型代码
        try {
            ModalityType.fromCode(command.getInputModalityCode());
        } catch (IllegalArgumentException e) {
            throw new DomainException("INVALID_INPUT_MODALITY", 
                "Invalid input modality: " + command.getInputModalityCode());
        }

        try {
            ModalityType.fromCode(command.getOutputModalityCode());
        } catch (IllegalArgumentException e) {
            throw new DomainException("INVALID_OUTPUT_MODALITY", 
                "Invalid output modality: " + command.getOutputModalityCode());
        }

        // 验证输入文件
        ModalityType inputModality = ModalityType.fromCode(command.getInputModalityCode());
        if (inputModality != ModalityType.TEXT && command.getInputFiles().isEmpty()) {
            throw new DomainException("MISSING_INPUT_FILES", 
                "Input files required for modality: " + inputModality.getCode());
        }

        // 验证文件大小
        long totalSize = command.getInputFiles().stream()
            .mapToLong(file -> file.getContent().length)
            .sum();
        if (totalSize > 100 * 1024 * 1024) { // 100MB
            throw new DomainException("FILE_SIZE_EXCEEDED", 
                "Total file size exceeds maximum limit (100MB)");
        }

        // 验证优先级
        if (command.getPriority() < 0 || command.getPriority() > 10) {
            throw new DomainException("INVALID_PRIORITY", 
                "Priority must be between 0 and 10");
        }
    }
}
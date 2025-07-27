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

package com.alibaba.cloud.ai.ddd.multimodal.interfaces.web;

import com.alibaba.cloud.ai.ddd.multimodal.application.command.CreateProcessingTaskCommand;
import com.alibaba.cloud.ai.ddd.multimodal.application.service.MultiModalApplicationService;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingStatus;
import com.alibaba.cloud.ai.ddd.multimodal.interfaces.dto.ProcessingTaskRequest;
import com.alibaba.cloud.ai.ddd.multimodal.interfaces.dto.ProcessingTaskResponse;
import com.alibaba.cloud.ai.application.annotation.UserIp;
import com.alibaba.cloud.ai.application.entity.result.Result;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DDD架构的多模态控制器
 * 基于领域驱动设计的统一多模态处理接口
 */
@RestController
@Tag(name = "DDD MultiModal APIs", description = "基于DDD架构的多模态处理接口")
@RequestMapping("/api/v1/ddd/multimodal")
public class DddMultiModalController {

    private static final Logger logger = LoggerFactory.getLogger(DddMultiModalController.class);
    
    private final MultiModalApplicationService applicationService;

    public DddMultiModalController(MultiModalApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 创建并处理多模态任务
     */
    @UserIp
    @PostMapping("/process")
    @Operation(summary = "创建并处理多模态任务", 
               description = "基于DDD架构的统一多模态处理接口")
    public Mono<Result<ProcessingTaskResponse>> processTask(
            @Parameter(description = "任务请求参数", required = true)
            @Validated @ModelAttribute ProcessingTaskRequest request,
            
            @Parameter(description = "上传的文件")
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            
            @Parameter(description = "用户ID")
            @RequestParam(value = "userId", defaultValue = "default-user") String userId
    ) {
        try {
            logger.info("Processing DDD multimodal task: {} for user: {}", request, userId);

            // 构建命令
            CreateProcessingTaskCommand command = buildCommand(request, files, userId);

            return applicationService.createAndProcessTask(command)
                    .map(result -> {
                        ProcessingTaskResponse response = ProcessingTaskResponse.fromResult(
                                result.getMetadata().get("taskId") != null ? 
                                result.getMetadata().get("taskId").toString() : "unknown",
                                result
                        );
                        return Result.success(response);
                    })
                    .onErrorResume(throwable -> {
                        logger.error("Error processing DDD multimodal task", throwable);
                        return Mono.just(handleError(throwable));
                    });

        } catch (Exception e) {
            logger.error("Error creating DDD multimodal task", e);
            return Mono.just(handleError(e));
        }
    }

    /**
     * 流式处理多模态任务
     */
    @UserIp
    @PostMapping(value = "/process-stream", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "流式处理多模态任务", 
               description = "支持流式输出的DDD多模态处理")
    public Flux<String> processTaskStream(
            @Validated @ModelAttribute ProcessingTaskRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "userId", defaultValue = "default-user") String userId
    ) {
        try {
            logger.info("Processing DDD multimodal task stream: {} for user: {}", request, userId);

            // 强制启用流式处理
            request.setStreaming(true);
            CreateProcessingTaskCommand command = buildCommand(request, files, userId);

            return applicationService.createAndProcessTaskStream(command)
                    .map(result -> result.getContent() != null ? result.getContent() : "")
                    .onErrorResume(throwable -> {
                        logger.error("Error in DDD stream processing", throwable);
                        return Flux.just("Error: " + throwable.getMessage());
                    });

        } catch (Exception e) {
            logger.error("Error creating DDD stream task", e);
            return Flux.just("Error: " + e.getMessage());
        }
    }

    /**
     * 处理二进制输出任务
     */
    @UserIp
    @PostMapping("/process-binary")
    @Operation(summary = "处理二进制输出任务", 
               description = "用于生成图像或音频等二进制内容")
    public Mono<Void> processBinaryTask(
            HttpServletResponse response,
            @Validated @ModelAttribute ProcessingTaskRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "userId", defaultValue = "default-user") String userId
    ) {
        try {
            CreateProcessingTaskCommand command = buildCommand(request, files, userId);

            return applicationService.createAndProcessTask(command)
                    .doOnNext(result -> {
                        try {
                            if (result.hasBinaryContent()) {
                                response.setContentType(result.getContentType());
                                response.getOutputStream().write(result.getBinaryContent());
                                response.getOutputStream().flush();
                            } else {
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.setContentType("text/plain");
                                response.getWriter().write(result.getContent() != null ? 
                                                         result.getContent() : "No content generated");
                            }
                        } catch (IOException e) {
                            logger.error("Error writing binary response", e);
                        }
                    })
                    .then()
                    .onErrorResume(throwable -> {
                        logger.error("Error in DDD binary processing", throwable);
                        return Mono.fromRunnable(() -> {
                            try {
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write("Binary processing failed: " + throwable.getMessage());
                            } catch (IOException ioException) {
                                logger.error("Error writing error response", ioException);
                            }
                        });
                    });

        } catch (Exception e) {
            logger.error("Error creating DDD binary task", e);
            return Mono.fromRunnable(() -> {
                try {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Task creation failed: " + e.getMessage());
                } catch (IOException ioException) {
                    logger.error("Error writing error response", ioException);
                }
            });
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务详情")
    public Mono<Result<ProcessingTaskResponse>> getTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId
    ) {
        return applicationService.getTask(taskId)
                .map(task -> Result.success(ProcessingTaskResponse.fromTask(task)))
                .onErrorResume(throwable -> {
                    logger.error("Error getting task: {}", taskId, throwable);
                    return Mono.just(handleError(throwable));
                });
    }

    /**
     * 获取用户任务列表
     */
    @GetMapping("/users/{userId}/tasks")
    @Operation(summary = "获取用户任务列表")
    public Mono<Result<List<ProcessingTaskResponse>>> getUserTasks(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String userId,
            
            @Parameter(description = "任务状态过滤")
            @RequestParam(value = "status", required = false) String status
    ) {
        Mono<List<ProcessingTask>> tasksMono;
        
        if (status != null) {
            try {
                ProcessingStatus processingStatus = ProcessingStatus.fromCode(status);
                tasksMono = applicationService.getUserTasksByStatus(userId, processingStatus);
            } catch (IllegalArgumentException e) {
                return Mono.just(Result.failed("Invalid status: " + status));
            }
        } else {
            tasksMono = applicationService.getUserTasks(userId);
        }

        return tasksMono
                .map(tasks -> {
                    List<ProcessingTaskResponse> responses = tasks.stream()
                            .map(ProcessingTaskResponse::fromTask)
                            .collect(Collectors.toList());
                    return Result.success(responses);
                })
                .onErrorResume(throwable -> {
                    logger.error("Error getting user tasks: {}", userId, throwable);
                    return Mono.just(handleError(throwable));
                });
    }

    /**
     * 取消任务
     */
    @PostMapping("/tasks/{taskId}/cancel")
    @Operation(summary = "取消任务")
    public Mono<Result<Void>> cancelTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId
    ) {
        return applicationService.cancelTask(taskId)
                .map(unused -> Result.<Void>success())
                .onErrorResume(throwable -> {
                    logger.error("Error cancelling task: {}", taskId, throwable);
                    return Mono.just(handleError(throwable));
                });
    }

    /**
     * 重试任务
     */
    @PostMapping("/tasks/{taskId}/retry")
    @Operation(summary = "重试失败的任务")
    public Mono<Result<ProcessingTaskResponse>> retryTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId
    ) {
        return applicationService.retryTask(taskId)
                .map(result -> {
                    ProcessingTaskResponse response = ProcessingTaskResponse.fromResult(taskId, result);
                    return Result.success(response);
                })
                .onErrorResume(throwable -> {
                    logger.error("Error retrying task: {}", taskId, throwable);
                    return Mono.just(handleError(throwable));
                });
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/system/status")
    @Operation(summary = "获取系统状态")
    public Mono<Result<Map<String, Object>>> getSystemStatus() {
        return applicationService.getSystemStatus()
                .map(status -> {
                    Map<String, Object> statusMap = Map.of(
                            "pendingTasks", status.pendingTasks(),
                            "processingTasks", status.processingTasks(),
                            "completedTasks", status.completedTasks(),
                            "failedTasks", status.failedTasks(),
                            "systemHealthy", status.systemHealthy(),
                            "availableEngines", status.availableEngines()
                    );
                    return Result.success(statusMap);
                })
                .onErrorResume(throwable -> {
                    logger.error("Error getting system status", throwable);
                    return Mono.just(handleError(throwable));
                });
    }

    /**
     * 构建命令对象
     */
    private CreateProcessingTaskCommand buildCommand(ProcessingTaskRequest request, 
                                                   MultipartFile[] files, String userId) {
        List<CreateProcessingTaskCommand.InputFileData> inputFiles = List.of();
        
        if (files != null && files.length > 0) {
            inputFiles = List.of(files).stream()
                    .filter(file -> !file.isEmpty())
                    .map(file -> {
                        try {
                            return new CreateProcessingTaskCommand.InputFileData(
                                    file.getOriginalFilename(),
                                    file.getBytes(),
                                    file.getContentType()
                            );
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to read file: " + file.getOriginalFilename(), e);
                        }
                    })
                    .collect(Collectors.toList());
        }

        return new CreateProcessingTaskCommand(
                userId,
                request.getInputModality(),
                request.getOutputModality(),
                request.getPrompt(),
                inputFiles,
                request.getParameters(),
                request.getPriority(),
                request.isStreaming()
        );
    }

    /**
     * 处理错误
     */
    private <T> Result<T> handleError(Throwable throwable) {
        if (throwable instanceof DomainException domainException) {
            return Result.failed(domainException.getMessage());
        } else {
            return Result.failed(throwable.getMessage());
        }
    }
}
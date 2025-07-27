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

package com.alibaba.cloud.ai.application.controller;

import com.alibaba.cloud.ai.application.annotation.UserIp;
import com.alibaba.cloud.ai.application.entity.result.Result;
import com.alibaba.cloud.ai.application.multimodal.core.ModalityType;
import com.alibaba.cloud.ai.application.multimodal.core.MultiModalOrchestrator;
import com.alibaba.cloud.ai.application.multimodal.model.MultiModalRequest;
import com.alibaba.cloud.ai.application.multimodal.model.MultiModalResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 统一多模态控制器
 * 提供统一的多模态处理接口，支持各种模态间的转换
 */
@RestController
@Tag(name = "Unified MultiModal APIs", description = "统一的多模态处理接口")
@RequestMapping("/api/v1/multimodal")
public class
MultiModalController {

    private static final Logger logger = LoggerFactory.getLogger(MultiModalController.class);
    
    private final MultiModalOrchestrator orchestrator;

    public MultiModalController(MultiModalOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * 统一多模态处理接口
     * 
     * @param prompt 用户输入的提示内容
     * @param inputModality 输入模态类型
     * @param outputModality 输出模态类型
     * @param files 上传的文件（可选）
     * @param parameters 额外参数（JSON格式）
     * @param streaming 是否使用流式处理
     * @return 处理结果
     */
    @UserIp
    @PostMapping("/process")
    @Operation(summary = "统一多模态处理", 
               description = "支持图像、音频、视频、文本等多种模态间的转换处理")
    public Mono<Result<MultiModalResponse>> process(
            @Parameter(description = "用户输入的提示内容", required = true)
            @RequestParam("prompt") String prompt,
            
            @Parameter(description = "输入模态类型：text, image, audio, video, document", required = true)
            @RequestParam("inputModality") String inputModality,
            
            @Parameter(description = "输出模态类型：text, image, audio, video", required = true)
            @RequestParam("outputModality") String outputModality,
            
            @Parameter(description = "上传的文件")
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            
            @Parameter(description = "额外参数，如style、resolution、voice等")
            @RequestParam(value = "parameters", required = false) Map<String, String> parameters,
            
            @Parameter(description = "是否使用流式处理")
            @RequestParam(value = "streaming", required = false, defaultValue = "false") boolean streaming
    ) {
        try {
            // 构建多模态请求
            MultiModalRequest request = new MultiModalRequest();
            request.setPrompt(prompt);
            request.setInputModality(ModalityType.valueOf(inputModality.toUpperCase()));
            request.setOutputModality(ModalityType.valueOf(outputModality.toUpperCase()));
            request.setStreaming(streaming);
            
            if (files != null && files.length > 0) {
                request.setFiles(Arrays.asList(files));
            }
            
            if (parameters != null) {
                parameters.forEach(request::addParameter);
            }
            
            logger.info("Processing multimodal request: {} -> {}, prompt length: {}, files: {}", 
                       inputModality, outputModality, prompt.length(), 
                       files != null ? files.length : 0);
            
            return orchestrator.process(request)
                    .map(response -> {
                        if (response.isSuccess()) {
                            return Result.<MultiModalResponse>success(response);
                        } else {
                            return Result.<MultiModalResponse>failed(response.getErrorMessage());
                        }
                    })
                    .onErrorResume(throwable -> {
                        logger.error("Error in multimodal processing", throwable);
                        return Mono.just(Result.failed("Processing failed: " + throwable.getMessage()));
                    });
                    
        } catch (Exception e) {
            logger.error("Error creating multimodal request", e);
            return Mono.just(Result.failed("Request creation failed: " + e.getMessage()));
        }
    }

    /**
     * 流式多模态处理接口
     */
    @UserIp
    @PostMapping(value = "/process-stream", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "流式多模态处理", 
               description = "支持流式输出的多模态处理，适用于长文本生成等场景")
    public Flux<String> processStream(
            @RequestParam("prompt") String prompt,
            @RequestParam("inputModality") String inputModality,
            @RequestParam("outputModality") String outputModality,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "parameters", required = false) Map<String, String> parameters
    ) {
        try {
            // 构建多模态请求
            MultiModalRequest request = new MultiModalRequest();
            request.setPrompt(prompt);
            request.setInputModality(ModalityType.valueOf(inputModality.toUpperCase()));
            request.setOutputModality(ModalityType.valueOf(outputModality.toUpperCase()));
            request.setStreaming(true);
            
            if (files != null && files.length > 0) {
                request.setFiles(Arrays.asList(files));
            }
            
            if (parameters != null) {
                parameters.forEach(request::addParameter);
            }
            
            logger.info("Processing multimodal stream request: {} -> {}, prompt length: {}", 
                       inputModality, outputModality, prompt.length());
            
            return orchestrator.processStream(request)
                    .map(response -> {
                        if (response.isSuccess()) {
                            return response.getContent() != null ? response.getContent() : "";
                        } else {
                            return "Error: " + response.getErrorMessage();
                        }
                    })
                    .onErrorResume(throwable -> {
                        logger.error("Error in stream processing", throwable);
                        return Flux.just("Error: " + throwable.getMessage());
                    });
                    
        } catch (Exception e) {
            logger.error("Error creating stream request", e);
            return Flux.just("Error: " + e.getMessage());
        }
    }

    /**
     * 批量多模态处理接口
     */
    @UserIp
    @PostMapping("/process-batch")
    @Operation(summary = "批量多模态处理", 
               description = "支持批量处理多个多模态请求")
    public Flux<Result<MultiModalResponse>> processBatch(
            @RequestBody List<BatchRequest> requests
    ) {
        try {
            Flux<MultiModalRequest> requestFlux = Flux.fromIterable(requests)
                    .map(batchReq -> {
                        MultiModalRequest request = new MultiModalRequest();
                        request.setPrompt(batchReq.prompt);
                        request.setInputModality(ModalityType.valueOf(batchReq.inputModality.toUpperCase()));
                        request.setOutputModality(ModalityType.valueOf(batchReq.outputModality.toUpperCase()));
                        
                        if (batchReq.parameters != null) {
                            batchReq.parameters.forEach(request::addParameter);
                        }
                        
                        return request;
                    });
            
            return orchestrator.processBatch(requestFlux)
                    .map(response -> {
                        if (response.isSuccess()) {
                            return Result.success(response);
                        } else {
                            return Result.failed(response.getErrorMessage());
                        }
                    });
                    
        } catch (Exception e) {
            logger.error("Error in batch processing", e);
            return Flux.just(Result.failed("Batch processing failed: " + e.getMessage()));
        }
    }

    /**
     * 获取系统支持的模态类型
     */
    @GetMapping("/modalities")
    @Operation(summary = "获取支持的模态类型", 
               description = "返回系统支持的所有输入和输出模态类型")
    public Result<Map<String, Object>> getSupportedModalities() {
        return Result.success(Map.of(
                "inputModalities", ModalityType.getInputSupportedTypes(),
                "outputModalities", ModalityType.getOutputSupportedTypes(),
                "processors", orchestrator.getProcessorInfo()
        ));
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "多模态服务健康检查")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "healthy",
                "processorsCount", orchestrator.getProcessorInfo().size(),
                "supportedModalities", ModalityType.values().length
        ));
    }

    /**
     * 处理二进制输出（如图像、音频）
     */
    @UserIp
    @PostMapping("/process-binary")
    @Operation(summary = "多模态处理（二进制输出）", 
               description = "用于生成图像或音频等二进制内容的多模态处理")
    public Mono<Void> processBinary(
            HttpServletResponse response,
            @RequestParam("prompt") String prompt,
            @RequestParam("inputModality") String inputModality,
            @RequestParam("outputModality") String outputModality,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "parameters", required = false) Map<String, String> parameters
    ) {
        try {
            // 构建多模态请求
            MultiModalRequest request = new MultiModalRequest();
            request.setPrompt(prompt);
            request.setInputModality(ModalityType.valueOf(inputModality.toUpperCase()));
            request.setOutputModality(ModalityType.valueOf(outputModality.toUpperCase()));
            
            if (files != null && files.length > 0) {
                request.setFiles(Arrays.asList(files));
            }
            
            if (parameters != null) {
                parameters.forEach(request::addParameter);
            }
            
            return orchestrator.process(request)
                    .doOnNext(result -> {
                        try {
                            if (result.isSuccess() && result.hasBinaryContent()) {
                                response.setContentType(result.getContentType());
                                response.getOutputStream().write(result.getBinaryContent());
                                response.getOutputStream().flush();
                            } else {
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write(result.getErrorMessage() != null ? 
                                                         result.getErrorMessage() : "No binary content generated");
                            }
                        } catch (IOException e) {
                            logger.error("Error writing binary response", e);
                        }
                    })
                    .then();
                    
        } catch (Exception e) {
            logger.error("Error in binary processing", e);
            return Mono.fromRunnable(() -> {
                try {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Binary processing failed: " + e.getMessage());
                } catch (IOException ioException) {
                    logger.error("Error writing error response", ioException);
                }
            });
        }
    }

    /**
     * 批量请求模型
     */
    public static class BatchRequest {
        public String prompt;
        public String inputModality;
        public String outputModality;
        public Map<String, String> parameters;
    }
}
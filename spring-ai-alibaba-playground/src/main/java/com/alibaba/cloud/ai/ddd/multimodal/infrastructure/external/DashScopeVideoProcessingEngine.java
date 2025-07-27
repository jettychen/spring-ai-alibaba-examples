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

package com.alibaba.cloud.ai.ddd.multimodal.infrastructure.external;

import com.alibaba.cloud.ai.ddd.multimodal.domain.model.*;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.ProcessingEngine;
import com.alibaba.cloud.ai.application.service.SAAVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * DashScope视频处理引擎实现
 * 适配现有的SAAVideoService到DDD架构
 */
@Component
public class DashScopeVideoProcessingEngine implements ProcessingEngine {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeVideoProcessingEngine.class);

    private final SAAVideoService videoService;
    private volatile boolean healthy = true;

    public DashScopeVideoProcessingEngine(SAAVideoService videoService) {
        this.videoService = videoService;
    }

    @Override
    public boolean supports(ProcessingTask task) {
        ModalityType input = task.getInputModality();
        ModalityType output = task.getOutputModality();
        
        // 仅支持视频到文本
        return input == ModalityType.VIDEO && output == ModalityType.TEXT;
    }

    @Override
    public Mono<ProcessingResult> process(ProcessingTask task) {
        logger.debug("Processing task: {} with DashScope Video Engine", task.getId());

        return Mono.fromCallable(() -> {
            try {
                return processVideoToText(task);
            } catch (Exception e) {
                logger.error("Error processing task: {}", task.getId(), e);
                throw new RuntimeException("Video processing failed: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public Flux<ProcessingResult> processStream(ProcessingTask task) {
        // 视频处理通常不支持流式，直接调用同步方法
        return process(task).flux();
    }

    @Override
    public int getPriority() {
        return 30; // 较低优先级，因为视频处理耗时较长
    }

    @Override
    public String getEngineName() {
        return "DashScope-Video-Engine";
    }

    @Override
    public long estimateProcessingTime(ProcessingTask task) {
        // 视频处理时间通常较长，根据文件大小估算
        long totalSize = task.getInputContents().stream()
                .mapToLong(InputContent::getSize)
                .sum();
        // 大概每MB需要1秒，最少10秒
        return Math.max(10000, totalSize / (1024 * 1024) * 1000);
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * 处理视频到文本转换
     */
    private ProcessingResult processVideoToText(ProcessingTask task) throws Exception {
        if (task.getInputContents().isEmpty()) {
            throw new IllegalArgumentException("Video content is required");
        }

        InputContent videoContent = task.getInputContents().get(0);
        String prompt = task.getPrompt().getContent();
        if (prompt.trim().isEmpty()) {
            prompt = "请总结这个视频的主要内容";
        }

        // 创建MockMultipartFile适配现有服务
        MockMultipartFile mockFile = new MockMultipartFile(
            "video",
            videoContent.getFileName(),
            videoContent.getContentType(),
            videoContent.getContent()
        );

        // 调用现有服务
        String result = videoService.analyzeVideo(prompt, mockFile);

        // 创建处理结果
        Map<String, Object> metadata = Map.of(
            "engine", getEngineName(),
            "originalFileName", videoContent.getFileName(),
            "fileSize", videoContent.getSize(),
            "videoFormat", getVideoFormat(videoContent),
            "estimatedDuration", estimateVideoDuration(videoContent),
            "analysisPrompt", prompt
        );

        return ProcessingResult.textResultWithMetadata(result, 0.8, metadata);
    }

    /**
     * 获取视频格式
     */
    private String getVideoFormat(InputContent videoContent) {
        String fileName = videoContent.getFileName();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        
        String contentType = videoContent.getContentType();
        if (contentType != null && contentType.startsWith("video/")) {
            return contentType.substring(6);
        }
        
        return "unknown";
    }

    /**
     * 估算视频时长（基于文件大小的粗略估算）
     */
    private String estimateVideoDuration(InputContent videoContent) {
        long fileSize = videoContent.getSize();
        // 假设平均比特率为 2Mbps
        long avgBitrate = 2 * 1024 * 1024 / 8;
        long estimatedSeconds = fileSize / avgBitrate;
        
        if (estimatedSeconds < 60) {
            return estimatedSeconds + "s (estimated)";
        } else if (estimatedSeconds < 3600) {
            return (estimatedSeconds / 60) + "m " + (estimatedSeconds % 60) + "s (estimated)";
        } else {
            long hours = estimatedSeconds / 3600;
            long minutes = (estimatedSeconds % 3600) / 60;
            long seconds = estimatedSeconds % 60;
            return hours + "h " + minutes + "m " + seconds + "s (estimated)";
        }
    }
}
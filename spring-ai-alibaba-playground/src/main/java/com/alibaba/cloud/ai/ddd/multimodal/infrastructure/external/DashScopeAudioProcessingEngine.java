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
import com.alibaba.cloud.ai.application.service.SAAAudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * DashScope音频处理引擎实现
 * 适配现有的SAAAudioService到DDD架构
 */
@Component
public class DashScopeAudioProcessingEngine implements ProcessingEngine {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeAudioProcessingEngine.class);

    private final SAAAudioService audioService;
    private volatile boolean healthy = true;

    public DashScopeAudioProcessingEngine(SAAAudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public boolean supports(ProcessingTask task) {
        ModalityType input = task.getInputModality();
        ModalityType output = task.getOutputModality();
        
        // 支持音频到文本和文本到音频
        return (input == ModalityType.AUDIO && output == ModalityType.TEXT) ||
               (input == ModalityType.TEXT && output == ModalityType.AUDIO);
    }

    @Override
    public Mono<ProcessingResult> process(ProcessingTask task) {
        logger.debug("Processing task: {} with DashScope Audio Engine", task.getId());

        return Mono.fromCallable(() -> {
            try {
                if (task.getInputModality() == ModalityType.AUDIO && 
                    task.getOutputModality() == ModalityType.TEXT) {
                    return processAudioToText(task);
                } else if (task.getInputModality() == ModalityType.TEXT && 
                          task.getOutputModality() == ModalityType.AUDIO) {
                    return processTextToAudio(task);
                } else {
                    throw new UnsupportedOperationException(
                        "Unsupported modality combination: " + 
                        task.getInputModality() + " -> " + task.getOutputModality());
                }
            } catch (Exception e) {
                logger.error("Error processing task: {}", task.getId(), e);
                throw new RuntimeException("Audio processing failed: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public Flux<ProcessingResult> processStream(ProcessingTask task) {
        // 音频处理通常不支持流式，直接调用同步方法
        return process(task).flux();
    }

    @Override
    public int getPriority() {
        return 20; // 相对较低优先级
    }

    @Override
    public String getEngineName() {
        return "DashScope-Audio-Engine";
    }

    @Override
    public long estimateProcessingTime(ProcessingTask task) {
        if (task.getInputModality() == ModalityType.AUDIO) {
            // 音频转文本，根据文件大小估算
            long totalSize = task.getInputContents().stream()
                    .mapToLong(InputContent::getSize)
                    .sum();
            return Math.max(2000, totalSize / 10000); // 最少2秒，大概每10KB需要1ms
        } else {
            // 文本转音频
            return Math.max(3000, task.getPrompt().length() * 50); // 最少3秒，每字符50ms
        }
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * 处理音频到文本转换
     */
    private ProcessingResult processAudioToText(ProcessingTask task) throws Exception {
        if (task.getInputContents().isEmpty()) {
            throw new IllegalArgumentException("Audio content is required");
        }

        InputContent audioContent = task.getInputContents().get(0);

        // 创建MockMultipartFile适配现有服务
        MockMultipartFile mockFile = new MockMultipartFile(
            "audio",
            audioContent.getFileName(),
            audioContent.getContentType(),
            audioContent.getContent()
        );

        // 调用现有服务
        String result = audioService.audio2text(mockFile);

        // 创建处理结果
        Map<String, Object> metadata = Map.of(
            "engine", getEngineName(),
            "originalFileName", audioContent.getFileName(),
            "fileSize", audioContent.getSize(),
            "audioFormat", getAudioFormat(audioContent),
            "estimatedDuration", estimateAudioDuration(audioContent)
        );

        return ProcessingResult.textResultWithMetadata(result, 0.85, metadata);
    }

    /**
     * 处理文本到音频转换
     */
    private ProcessingResult processTextToAudio(ProcessingTask task) {
        String prompt = task.getPrompt().getContent();
        if (prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Text is required for text to audio synthesis");
        }

        String voice = task.getParameter("voice", "default");
        String language = task.getParameter("language", "zh-CN");

        logger.debug("Synthesizing audio with text: {}, voice: {}, language: {}", 
                    prompt.length(), voice, language);

        // 调用现有服务
        byte[] audioData = audioService.text2audio(prompt);

        Map<String, Object> metadata = Map.of(
            "engine", getEngineName(),
            "voice", voice,
            "language", language,
            "textLength", prompt.length()
        );

        if (audioData != null && audioData.length > 0) {
            // 检查是否是占位符响应
            String dataString = new String(audioData);
            if ("not implemented yet".equals(dataString)) {
                return ProcessingResult.textResultWithMetadata(
                    "Text to audio synthesis is not yet implemented", 0.0, metadata);
            }
            
            return ProcessingResult.binaryResultWithMetadata(
                audioData, "audio/wav", 0.9, metadata);
        } else {
            return ProcessingResult.textResultWithMetadata(
                "Failed to generate audio data", 0.0, metadata);
        }
    }

    /**
     * 获取音频格式
     */
    private String getAudioFormat(InputContent audioContent) {
        String fileName = audioContent.getFileName();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        
        String contentType = audioContent.getContentType();
        if (contentType != null && contentType.startsWith("audio/")) {
            return contentType.substring(6);
        }
        
        return "unknown";
    }

    /**
     * 估算音频时长（基于文件大小的粗略估算）
     */
    private String estimateAudioDuration(InputContent audioContent) {
        long fileSize = audioContent.getSize();
        // 假设平均比特率为 128kbps
        long estimatedSeconds = fileSize / (128 * 1024 / 8);
        return estimatedSeconds + "s (estimated)";
    }
}
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

package com.alibaba.cloud.ai.application.multimodal.processor;

import com.alibaba.cloud.ai.application.multimodal.core.ModalityType;
import com.alibaba.cloud.ai.application.multimodal.core.MultiModalProcessor;
import com.alibaba.cloud.ai.application.multimodal.model.MultiModalRequest;
import com.alibaba.cloud.ai.application.multimodal.model.MultiModalResponse;
import com.alibaba.cloud.ai.application.service.SAAAudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 音频处理器
 * 处理音频到文本和文本到音频的转换
 */
@Component
public class AudioProcessor implements MultiModalProcessor<MultiModalRequest, MultiModalResponse> {

    private static final Logger logger = LoggerFactory.getLogger(AudioProcessor.class);
    
    private final SAAAudioService audioService;

    public AudioProcessor(SAAAudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public ModalityType getSupportedModality() {
        return ModalityType.AUDIO;
    }

    @Override
    public boolean supports(MultiModalRequest request) {
        return request != null && (
                (request.getInputModality() == ModalityType.AUDIO && request.getOutputModality() == ModalityType.TEXT) ||
                (request.getInputModality() == ModalityType.TEXT && request.getOutputModality() == ModalityType.AUDIO)
        );
    }

    @Override
    public Mono<MultiModalResponse> process(MultiModalRequest request) {
        if (request.getInputModality() == ModalityType.AUDIO && request.getOutputModality() == ModalityType.TEXT) {
            return processAudioToText(request);
        } else if (request.getInputModality() == ModalityType.TEXT && request.getOutputModality() == ModalityType.AUDIO) {
            return processTextToAudio(request);
        }
        
        return Mono.just(MultiModalResponse.error(request.getRequestId(), 
                "Unsupported modality combination: " + request.getInputModality() + " -> " + request.getOutputModality()));
    }

    @Override
    public Flux<MultiModalResponse> processStream(MultiModalRequest request) {
        // 音频处理通常不支持流式，直接调用同步方法
        return process(request).flux();
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public MultiModalRequest preProcess(MultiModalRequest request) {
        // 验证音频文件
        if (request.getInputModality() == ModalityType.AUDIO) {
            if (!request.hasFiles()) {
                throw new IllegalArgumentException("Audio file is required for audio processing");
            }
            
            MultipartFile audioFile = request.getFirstFile();
            if (audioFile.isEmpty()) {
                throw new IllegalArgumentException("Audio file cannot be empty");
            }
            
            // 验证文件类型
            String contentType = audioFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                // 也允许一些视频格式，因为它们可能包含音频
                if (contentType == null || (!contentType.startsWith("video/") && !contentType.equals("application/octet-stream"))) {
                    logger.warn("Potentially invalid audio file type: {}", contentType);
                }
            }
        }
        
        // 设置默认参数
        if (request.getParameter("language", String.class) == null) {
            request.addParameter("language", "zh-CN");
        }
        if (request.getParameter("voice", String.class) == null) {
            request.addParameter("voice", "default");
        }
        
        return request;
    }

    @Override
    public MultiModalResponse postProcess(MultiModalResponse response, MultiModalRequest request) {
        // 添加处理元数据
        response.addMetadata("processor", "AudioProcessor");
        response.addMetadata("inputModality", request.getInputModality().getCode());
        response.addMetadata("outputModality", request.getOutputModality().getCode());
        
        if (request.hasFiles()) {
            MultipartFile file = request.getFirstFile();
            response.addMetadata("originalFileName", file.getOriginalFilename());
            response.addMetadata("fileSize", file.getSize());
            response.addMetadata("contentType", file.getContentType());
        }
        
        return response;
    }

    /**
     * 处理音频到文本转换
     */
    private Mono<MultiModalResponse> processAudioToText(MultiModalRequest request) {
        return Mono.fromCallable(() -> {
            try {
                MultipartFile audioFile = request.getFirstFile();
                
                logger.debug("Processing audio to text: requestId={}, fileName={}", 
                           request.getRequestId(), audioFile.getOriginalFilename());
                
                // 使用现有的音频服务
                String result = audioService.audio2text(audioFile);
                
                MultiModalResponse response = MultiModalResponse.success(
                        request.getRequestId(), 
                        ModalityType.TEXT, 
                        result
                );
                response.setConfidence(0.85); // 设置置信度
                response.addMetadata("audioFormat", getAudioFormat(audioFile));
                response.addMetadata("audioDuration", estimateAudioDuration(audioFile));
                
                return response;
                
            } catch (Exception e) {
                logger.error("Error processing audio to text: requestId={}", request.getRequestId(), e);
                return MultiModalResponse.error(request.getRequestId(), "Audio transcription failed: " + e.getMessage());
            }
        });
    }

    /**
     * 处理文本到音频转换
     */
    private Mono<MultiModalResponse> processTextToAudio(MultiModalRequest request) {
        return Mono.fromCallable(() -> {
            try {
                String prompt = request.getPrompt();
                if (prompt == null || prompt.trim().isEmpty()) {
                    throw new IllegalArgumentException("Text is required for text to audio synthesis");
                }
                
                String voice = request.getParameter("voice", String.class);
                String language = request.getParameter("language", String.class);
                
                logger.debug("Processing text to audio: requestId={}, textLength={}, voice={}, language={}", 
                           request.getRequestId(), prompt.length(), voice, language);
                
                // 注意：原始服务返回 "not implemented yet".getBytes()
                // 这里检查是否已经实现了真正的功能
                byte[] audioData = audioService.text2audio(prompt);
                
                if (audioData != null && audioData.length > 0) {
                    // 检查是否是占位符响应
                    String dataString = new String(audioData);
                    if ("not implemented yet".equals(dataString)) {
                        return MultiModalResponse.error(request.getRequestId(), 
                                "Text to audio synthesis is not yet implemented");
                    }
                    
                    MultiModalResponse response = MultiModalResponse.success(
                            request.getRequestId(), 
                            ModalityType.AUDIO, 
                            audioData,
                            "audio/wav"
                    );
                    response.setConfidence(0.9);
                    response.addMetadata("voice", voice);
                    response.addMetadata("language", language);
                    response.addMetadata("textLength", prompt.length());
                    
                    return response;
                } else {
                    return MultiModalResponse.error(request.getRequestId(), 
                            "Failed to generate audio data");
                }
                
            } catch (Exception e) {
                logger.error("Error processing text to audio: requestId={}", request.getRequestId(), e);
                return MultiModalResponse.error(request.getRequestId(), "Audio synthesis failed: " + e.getMessage());
            }
        });
    }

    /**
     * 获取音频格式
     */
    private String getAudioFormat(MultipartFile audioFile) {
        String fileName = audioFile.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        
        String contentType = audioFile.getContentType();
        if (contentType != null && contentType.startsWith("audio/")) {
            return contentType.substring(6);
        }
        
        return "unknown";
    }

    /**
     * 估算音频时长（基于文件大小的粗略估算）
     */
    private String estimateAudioDuration(MultipartFile audioFile) {
        // 这是一个粗略的估算，实际应该解析音频文件头信息
        long fileSize = audioFile.getSize();
        // 假设平均比特率为 128kbps
        long estimatedSeconds = fileSize / (128 * 1024 / 8);
        return estimatedSeconds + "s (estimated)";
    }
}
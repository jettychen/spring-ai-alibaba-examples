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
import com.alibaba.cloud.ai.application.service.SAAVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 视频处理器
 * 处理视频到文本的转换和视频内容分析
 */
@Component
public class VideoProcessor implements MultiModalProcessor<MultiModalRequest, MultiModalResponse> {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);
    
    private final SAAVideoService videoService;

    public VideoProcessor(SAAVideoService videoService) {
        this.videoService = videoService;
    }

    @Override
    public ModalityType getSupportedModality() {
        return ModalityType.VIDEO;
    }

    @Override
    public boolean supports(MultiModalRequest request) {
        return request != null && 
               request.getInputModality() == ModalityType.VIDEO && 
               request.getOutputModality() == ModalityType.TEXT;
    }

    @Override
    public Mono<MultiModalResponse> process(MultiModalRequest request) {
        return processVideoToText(request);
    }

    @Override
    public Flux<MultiModalResponse> processStream(MultiModalRequest request) {
        // 视频处理通常不支持流式，直接调用同步方法
        return process(request).flux();
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public MultiModalRequest preProcess(MultiModalRequest request) {
        // 验证视频文件
        if (!request.hasFiles()) {
            throw new IllegalArgumentException("Video file is required for video processing");
        }
        
        MultipartFile videoFile = request.getFirstFile();
        if (videoFile.isEmpty()) {
            throw new IllegalArgumentException("Video file cannot be empty");
        }
        
        // 验证文件类型
        String contentType = videoFile.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            // 也允许一些通用的二进制类型，因为视频文件可能被标记为这样
            if (contentType == null || !contentType.equals("application/octet-stream")) {
                logger.warn("Potentially invalid video file type: {}", contentType);
            }
        }
        
        // 验证文件大小（视频文件通常较大）
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (videoFile.getSize() > maxSize) {
            throw new IllegalArgumentException("Video file size exceeds maximum limit of " + (maxSize / 1024 / 1024) + "MB");
        }
        
        // 设置默认参数
        if (request.getParameter("analysisType", String.class) == null) {
            request.addParameter("analysisType", "summary");
        }
        if (request.getParameter("language", String.class) == null) {
            request.addParameter("language", "zh-CN");
        }
        
        return request;
    }

    @Override
    public MultiModalResponse postProcess(MultiModalResponse response, MultiModalRequest request) {
        // 添加处理元数据
        response.addMetadata("processor", "VideoProcessor");
        response.addMetadata("inputModality", request.getInputModality().getCode());
        response.addMetadata("outputModality", request.getOutputModality().getCode());
        
        if (request.hasFiles()) {
            MultipartFile file = request.getFirstFile();
            response.addMetadata("originalFileName", file.getOriginalFilename());
            response.addMetadata("fileSize", file.getSize());
            response.addMetadata("contentType", file.getContentType());
            response.addMetadata("videoFormat", getVideoFormat(file));
        }
        
        String analysisType = request.getParameter("analysisType", String.class);
        response.addMetadata("analysisType", analysisType);
        
        return response;
    }

    /**
     * 处理视频到文本转换
     */
    private Mono<MultiModalResponse> processVideoToText(MultiModalRequest request) {
        return Mono.fromCallable(() -> {
            try {
                MultipartFile videoFile = request.getFirstFile();
                String prompt = request.getPrompt();
                if (prompt == null || prompt.trim().isEmpty()) {
                    prompt = "请总结这个视频的主要内容";
                }
                
                String analysisType = request.getParameter("analysisType", String.class);
                
                logger.debug("Processing video to text: requestId={}, fileName={}, prompt={}, analysisType={}", 
                           request.getRequestId(), videoFile.getOriginalFilename(), prompt, analysisType);
                
                // 使用现有的视频服务
                String result = videoService.analyzeVideo(prompt, videoFile);
                
                MultiModalResponse response = MultiModalResponse.success(
                        request.getRequestId(), 
                        ModalityType.TEXT, 
                        result
                );
                response.setConfidence(0.8); // 视频分析的置信度相对较低
                response.addMetadata("analysisPrompt", prompt);
                response.addMetadata("videoDuration", estimateVideoDuration(videoFile));
                response.addMetadata("videoSize", formatFileSize(videoFile.getSize()));
                
                return response;
                
            } catch (Exception e) {
                logger.error("Error processing video to text: requestId={}", request.getRequestId(), e);
                return MultiModalResponse.error(request.getRequestId(), "Video analysis failed: " + e.getMessage());
            }
        });
    }

    /**
     * 获取视频格式
     */
    private String getVideoFormat(MultipartFile videoFile) {
        String fileName = videoFile.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        
        String contentType = videoFile.getContentType();
        if (contentType != null && contentType.startsWith("video/")) {
            return contentType.substring(6);
        }
        
        return "unknown";
    }

    /**
     * 估算视频时长（基于文件大小的粗略估算）
     */
    private String estimateVideoDuration(MultipartFile videoFile) {
        // 这是一个非常粗略的估算，实际应该解析视频文件的元数据
        long fileSize = videoFile.getSize();
        // 假设平均比特率为 2Mbps (2 * 1024 * 1024 / 8 bytes per second)
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

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
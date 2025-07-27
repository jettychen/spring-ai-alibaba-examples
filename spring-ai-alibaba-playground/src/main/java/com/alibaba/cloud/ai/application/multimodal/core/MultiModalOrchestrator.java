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

package com.alibaba.cloud.ai.application.multimodal.core;

import com.alibaba.cloud.ai.application.multimodal.model.MultiModalRequest;
import com.alibaba.cloud.ai.application.multimodal.model.MultiModalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 多模态处理编排器
 * 负责协调和管理所有多模态处理器，提供统一的处理入口
 */
@Component
public class MultiModalOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(MultiModalOrchestrator.class);
    
    private final List<MultiModalProcessor<?, ?>> processors;
    private final Duration defaultTimeout = Duration.ofMinutes(5);

    public MultiModalOrchestrator(List<MultiModalProcessor<?, ?>> processors) {
        this.processors = processors;
        // 按优先级排序处理器
        this.processors.sort(Comparator.comparingInt(MultiModalProcessor::getPriority));
        logger.info("Initialized MultiModalOrchestrator with {} processors", processors.size());
    }

    /**
     * 处理多模态请求
     * @param request 多模态请求
     * @return 处理结果
     */
    @SuppressWarnings("unchecked")
    public Mono<MultiModalResponse> process(MultiModalRequest request) {
        logger.debug("Processing multimodal request: {}", request);
        
        return findProcessor(request)
                .cast(MultiModalProcessor.class)
                .flatMap(processor -> {
                    long startTime = System.currentTimeMillis();
                    
                    return processor.process(processor.preProcess(request))
                            .cast(MultiModalResponse.class)
                            .map(response -> {
                                @SuppressWarnings("unchecked")
                                MultiModalResponse finalResponse = ((MultiModalProcessor<MultiModalRequest, MultiModalResponse>) processor)
                                        .postProcess((MultiModalResponse) response, request);
                                finalResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                                logger.debug("Successfully processed request {} in {}ms", 
                                           request.getRequestId(), finalResponse.getProcessingTimeMs());
                                return finalResponse;
                            })
                            .timeout(defaultTimeout)
                            .onErrorResume(throwable -> {
                                logger.error("Error processing request: {}", request.getRequestId(), throwable);
                                String errorMessage = throwable instanceof Throwable ? 
                                                     ((Throwable) throwable).getMessage() : 
                                                     "Unknown error";
                                return Mono.just(MultiModalResponse.error(request.getRequestId(), 
                                                                        "Processing failed: " + errorMessage));
                            });
                })
                .switchIfEmpty(Mono.just(MultiModalResponse.error(request.getRequestId(), 
                                                                "No suitable processor found for this request")));
    }

    /**
     * 流式处理多模态请求
     * @param request 多模态请求
     * @return 流式处理结果
     */
    @SuppressWarnings("unchecked")
    public Flux<MultiModalResponse> processStream(MultiModalRequest request) {
        logger.debug("Processing multimodal request stream: {}", request);
        
        return findProcessor(request)
                .cast(MultiModalProcessor.class)
                .flatMapMany(processor -> {
                    long startTime = System.currentTimeMillis();
                    
                    return processor.processStream(processor.preProcess(request))
                            .cast(MultiModalResponse.class)
                            .map(response -> {
                                @SuppressWarnings("unchecked")
                                MultiModalResponse finalResponse = ((MultiModalProcessor<MultiModalRequest, MultiModalResponse>) processor)
                                        .postProcess((MultiModalResponse) response, request);
                                finalResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                                return finalResponse;
                            })
                            .timeout(defaultTimeout)
                            .onErrorResume(throwable -> {
                                logger.error("Error processing stream request: {}", request.getRequestId(), throwable);
                                String errorMessage = throwable instanceof Throwable ? 
                                                     ((Throwable) throwable).getMessage() : 
                                                     "Unknown error";
                                return Flux.just(MultiModalResponse.error(request.getRequestId(), 
                                                                        "Stream processing failed: " + errorMessage));
                            });
                })
                .switchIfEmpty(Flux.just(MultiModalResponse.error(request.getRequestId(), 
                                                                "No suitable processor found for this request")));
    }

    /**
     * 批量处理多模态请求
     * @param requests 多模态请求流
     * @return 批量处理结果流
     */
    public Flux<MultiModalResponse> processBatch(Flux<MultiModalRequest> requests) {
        return requests
                .flatMap(this::process)
                .onErrorContinue((throwable, obj) -> {
                    logger.error("Error in batch processing", throwable);
                });
    }

    /**
     * 查找合适的处理器
     * @param request 多模态请求
     * @return 匹配的处理器
     */
    private Mono<MultiModalProcessor<?, ?>> findProcessor(MultiModalRequest request) {
        return Mono.fromCallable(() -> 
                processors.stream()
                        .filter(processor -> processor.supports(request))
                        .findFirst()
        ).flatMap(optionalProcessor -> 
                optionalProcessor.map(Mono::just)
                               .orElse(Mono.empty())
        );
    }

    /**
     * 获取所有注册的处理器信息
     * @return 处理器信息列表
     */
    public List<ProcessorInfo> getProcessorInfo() {
        return processors.stream()
                .map(processor -> new ProcessorInfo(
                        processor.getClass().getSimpleName(),
                        processor.getSupportedModality(),
                        processor.getPriority()
                ))
                .toList();
    }

    /**
     * 检查指定模态是否有可用的处理器
     * @param modality 模态类型
     * @return 是否有可用处理器
     */
    public boolean hasProcessorFor(ModalityType modality) {
        return processors.stream()
                .anyMatch(processor -> processor.getSupportedModality() == modality);
    }

    /**
     * 处理器信息记录
     */
    public record ProcessorInfo(String name, ModalityType supportedModality, int priority) {}
}
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * DashScope文本处理引擎实现
 * 处理文本到文本的转换
 */
@Component
public class DashScopeTextProcessingEngine implements ProcessingEngine {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeTextProcessingEngine.class);

    private final ChatClient chatClient;
    private volatile boolean healthy = true;

    public DashScopeTextProcessingEngine(@Qualifier("dashscopeChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public boolean supports(ProcessingTask task) {
        ModalityType input = task.getInputModality();
        ModalityType output = task.getOutputModality();
        
        // 支持文本到文本的处理
        return input == ModalityType.TEXT && output == ModalityType.TEXT;
    }

    @Override
    public Mono<ProcessingResult> process(ProcessingTask task) {
        logger.info("Processing text task: {}", task.getId());

        try {
            String prompt = task.getPrompt().getContent();
            
            return Mono.fromCallable(() -> {
                String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
                
                return ProcessingResult.textResult(response, 1.0);
            });
            
        } catch (Exception e) {
            logger.error("Failed to process text task: {}", task.getId(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<ProcessingResult> processStream(ProcessingTask task) {
        logger.info("Processing text task with streaming: {}", task.getId());

        try {
            String prompt = task.getPrompt().getContent();
            
            return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .map(chunk -> ProcessingResult.textResult(chunk, 1.0));
                
        } catch (Exception e) {
            logger.error("Failed to process streaming text task: {}", task.getId(), e);
            return Flux.error(e);
        }
    }

    @Override
    public int getPriority() {
        return 10; // 高优先级用于文本处理
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    @Override
    public String getEngineName() {
        return "DashScope-Text-Engine";
    }

    @Override
    public long estimateProcessingTime(ProcessingTask task) {
        // 估计文本处理时间（毫秒）
        String prompt = task.getPrompt().getContent();
        int wordCount = prompt.split("\\s+").length;
        return Math.max(1000, wordCount * 100); // 基于词数估算，最少1秒
    }

}
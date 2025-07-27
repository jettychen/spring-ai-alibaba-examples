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

package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.book.infrastructure.external.BookMultiModalProcessor;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingResult;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ModalityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * 处理编排服务
 * 负责选择合适的处理引擎并编排处理流程
 */
@Service
public class ProcessingOrchestrator {

    private final List<ProcessingEngine> engines;
    private final IntentRecognizer intentRecognizer;
    private final BookIntentSupportStrategy bookIntentSupportStrategy;
    private final GeneralIntentSupportStrategy generalIntentSupportStrategy;

    @Autowired
    public ProcessingOrchestrator(List<ProcessingEngine> engines, 
                                 IntentRecognizer intentRecognizer,
                                 BookIntentSupportStrategy bookIntentSupportStrategy,
                                 GeneralIntentSupportStrategy generalIntentSupportStrategy) {
        this.engines = engines;
        this.intentRecognizer = intentRecognizer;
        this.bookIntentSupportStrategy = bookIntentSupportStrategy;
        this.generalIntentSupportStrategy = generalIntentSupportStrategy;
    }

    /**
     * 为任务选择最合适的处理引擎
     *
     * @param task 处理任务
     * @return 处理引擎
     */
    public Optional<ProcessingEngine> selectEngine(ProcessingTask task) {
        // 首先根据用户意图选择引擎
        IntentRecognizer.UserIntent intent = intentRecognizer.recognizeIntent(task);
        
        // 尝试找到专门处理该意图的引擎
        Optional<ProcessingEngine> intentBasedEngine = engines.stream()
                .filter(engine -> isIntentSupported(engine, intent))
                .filter(ProcessingEngine::isHealthy)
                .findFirst();
        
        // 如果找到了专门处理该意图的引擎，则使用它
        if (intentBasedEngine.isPresent()) {
            return intentBasedEngine;
        }
        
        // 否则，回退到原有的基于模态类型的支持性检查
        return engines.stream()
                .filter(engine -> engine.supports(task))
                .filter(ProcessingEngine::isHealthy)
                .findFirst();
    }

    /**
     * 处理任务
     *
     * @param task 处理任务
     * @return 处理结果
     */
    public Mono<ProcessingResult> processTask(ProcessingTask task) {
        // 首先根据用户意图选择引擎，并获取参数
        IntentRecognitionResult intentResult = intentRecognizer.recognizeIntentWithParameters(task);
        IntentRecognizer.UserIntent intent = intentResult.getIntent();
        
        // 将识别出的参数合并到任务参数中，包括意图
        Map<String, String> additionalParameters = new HashMap<>(intentResult.getParameters());
        additionalParameters.put("intent", intent.name());
        ProcessingTask taskWithParameters = mergeParameters(task, additionalParameters);
        
        // 尝试找到专门处理该意图的引擎
        Optional<ProcessingEngine> intentBasedEngine = engines.stream()
                .filter(engine -> isIntentSupported(engine, intent))
                .filter(ProcessingEngine::isHealthy)
                .findFirst();
        
        ProcessingEngine engineToUse;
        if (intentBasedEngine.isPresent()) {
            engineToUse = intentBasedEngine.get();
        } else {
            // 否则，回退到原有的基于模态类型的支持性检查
            Optional<ProcessingEngine> fallbackEngine = engines.stream()
                    .filter(engine -> engine.supports(taskWithParameters))
                    .filter(ProcessingEngine::isHealthy)
                    .findFirst();
            
            if (fallbackEngine.isPresent()) {
                engineToUse = fallbackEngine.get();
            } else {
                return Mono.error(new RuntimeException("No suitable processing engine found for task"));
            }
        }
        
        // 如果是图书管理处理器且意图明确，使用特定的处理方法
        if (engineToUse instanceof BookMultiModalProcessor) {
            BookMultiModalProcessor bookProcessor = (BookMultiModalProcessor) engineToUse;
            if (intent != IntentRecognizer.UserIntent.GENERAL_PROCESSING) {
                // 使用带有意图的处理方法
                return bookProcessor.process(taskWithParameters, intent);
            } else {
                // 使用通用处理方法
                return bookProcessor.processWithIntent(taskWithParameters, intent);
            }
        } else {
            // 否则使用通用处理方法
            return engineToUse.process(taskWithParameters);
        }
    }
    
    /**
     * 合并识别出的参数到任务参数中
     * @param task 原始任务
     * @param recognizedParameters 识别出的参数
     * @return 合并参数后的任务
     */
    private ProcessingTask mergeParameters(ProcessingTask task, Map<String, String> recognizedParameters) {
        if (recognizedParameters == null || recognizedParameters.isEmpty()) {
            return task;
        }
        
        // 创建新的参数映射，包含原有的参数和识别出的新参数
        Map<String, String> mergedParameters = new HashMap<>(task.getParameters());
        mergedParameters.putAll(recognizedParameters);
        
        // 使用Builder模式创建新的任务对象（这里简化处理，实际应该使用Builder模式）
        // 由于ProcessingTask是不可变对象，我们需要创建一个新的实例
        return ProcessingTask.create(
                task.getId(),
                task.getUserId(),
                task.getInputModality(),
                task.getOutputModality(),
                task.getPrompt(),
                task.getInputContents(),
                mergedParameters,
                task.getPriority()
        );
    }

    /**
     * 检查引擎是否支持特定意图
     * 
     * @param engine 处理引擎
     * @param intent 用户意图
     * @return 是否支持
     */
    private boolean isIntentSupported(ProcessingEngine engine, IntentRecognizer.UserIntent intent) {
        // 使用策略模式来判断引擎是否支持特定意图
        switch (intent) {
            case VIEW_AVAILABLE_BOOKS:
            case SEARCH_BOOKS:
            case BORROW_BOOK_LIST:
            case BORROW_BOOK_ACTION:
            case RETURN_BOOK:
                // 图书相关意图使用图书意图支持策略
                return bookIntentSupportStrategy.isSupported(engine);
            case GENERAL_PROCESSING:
                // 通用意图使用通用意图支持策略
                return generalIntentSupportStrategy.isSupported(engine);
            default:
                // 默认情况下，可以使用任何引擎
                return true;
        }
    }

    /**
     * 检查是否支持指定的模态转换
     *
     * @param inputModality 输入模态
     * @param outputModality 输出模态
     * @return 是否支持
     */
    public boolean supportsModalityConversion(ModalityType inputModality, ModalityType outputModality) {
        return engines.stream()
                .anyMatch(engine -> {
                    // 创建一个临时任务来检查支持性
                    ProcessingTask tempTask = ProcessingTask.create(
                            com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId.generate(),
                            "temp",
                            inputModality,
                            outputModality,
                            com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingPrompt.defaultFor(inputModality),
                            List.of(),
                            java.util.Map.of(),
                            0
                    );
                    return engine.supports(tempTask) && engine.isHealthy();
                });
    }

    /**
     * 获取所有可用的处理引擎信息
     *
     * @return 引擎信息列表
     */
    public List<EngineInfo> getAvailableEngines() {
        return engines.stream()
                .map(engine -> new EngineInfo(
                        engine.getEngineName(),
                        engine.getPriority(),
                        engine.isHealthy()
                ))
                .toList();
    }

    /**
     * 获取系统整体健康状态
     *
     * @return 是否健康
     */
    public boolean isSystemHealthy() {
        return engines.stream().anyMatch(ProcessingEngine::isHealthy);
    }

    /**
     * 获取可用引擎数量
     *
     * @return 可用引擎数量
     */
    public int getAvailableEngineCount() {
        return (int) engines.stream().filter(ProcessingEngine::isHealthy).count();
    }

    /**
     * 引擎信息记录
     */
    public record EngineInfo(String name, int priority, boolean healthy) {}
}
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

package com.alibaba.cloud.ai.ddd.multimodal.config;

import com.alibaba.cloud.ai.ddd.multimodal.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * DDD多模态配置类
 * 配置DDD架构相关的Bean
 */
@Configuration
public class DddMultiModalConfiguration {

    /**
     * 配置处理编排器
     * 自动注入所有可用的处理引擎
     */
    @Bean
    public ProcessingOrchestrator processingOrchestrator(
            List<ProcessingEngine> engines, 
            IntentRecognizer intentRecognizer,
            BookIntentSupportStrategy bookIntentSupportStrategy,
            GeneralIntentSupportStrategy generalIntentSupportStrategy) {
        return new ProcessingOrchestrator(engines, intentRecognizer, bookIntentSupportStrategy, generalIntentSupportStrategy);
    }
}
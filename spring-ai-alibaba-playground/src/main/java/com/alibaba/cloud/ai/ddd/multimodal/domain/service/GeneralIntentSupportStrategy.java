package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.book.infrastructure.external.BookMultiModalProcessor;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;
import org.springframework.stereotype.Component;

/**
 * 通用意图支持策略
 * 处理通用意图支持判断逻辑
 */
@Component
public class GeneralIntentSupportStrategy implements IntentSupportStrategy {

    /**
     * 判断当前策略是否支持指定的处理引擎
     *
     * @param engine 要检查的处理引擎
     * @return 如果处理引擎是 BookMultiModalProcessor 类型则不支持，其他情况均支持
     */
    @Override
    public boolean isSupported(ProcessingEngine engine) {
        // 除了图书多模态处理器外，其他处理器都支持通用意图
        return !(engine instanceof BookMultiModalProcessor);
    }

    /**
     * 获取当前策略对应意图类型
     *
     * @return 始终返回 null，因为策略选择是基于意图类型而不是策略类型
     */
    @Override
    public UserIntent getIntentType() {
        // 这个方法在当前实现中不会被使用，因为策略选择是基于意图类型而不是策略类型
        return null;
    }
    
    /**
     * 检查特定意图是否被支持
     * @param intent 用户意图
     * @return 是否支持
     */
    public boolean isIntentSupported(IntentRecognizer.UserIntent intent) {
        // 通用意图支持策略只支持通用处理意图
        return intent == UserIntent.GENERAL_PROCESSING;
    }
}
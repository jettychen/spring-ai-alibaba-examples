package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.book.infrastructure.external.BookMultiModalProcessor;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;
import org.springframework.stereotype.Component;

/**
 * 图书相关意图支持策略
 * 处理与图书管理相关的意图支持判断逻辑
 */
@Component
public class BookIntentSupportStrategy implements IntentSupportStrategy {
    
    @Override
    public boolean isSupported(ProcessingEngine engine) {
        // 只有图书多模态处理器才支持图书相关意图
        return engine instanceof BookMultiModalProcessor;
    }
    
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
        // 支持所有图书相关的意图
        return intent == UserIntent.VIEW_AVAILABLE_BOOKS ||
               intent == UserIntent.SEARCH_BOOKS ||
               intent == UserIntent.BORROW_BOOK_LIST ||
               intent == UserIntent.BORROW_BOOK_ACTION ||
               intent == UserIntent.RETURN_BOOK;
    }
}
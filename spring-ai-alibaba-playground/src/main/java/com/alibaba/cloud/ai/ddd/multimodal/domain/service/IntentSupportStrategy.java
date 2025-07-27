package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;

/**
 * 意图支持策略接口
 * 定义不同意图类型对处理引擎支持的判断逻辑
 */
public interface IntentSupportStrategy {
    
    /**
     * 检查处理引擎是否支持特定意图
     * 
     * @param engine 处理引擎
     * @return 是否支持
     */
    boolean isSupported(ProcessingEngine engine);
    
    /**
     * 获取策略适用的意图类型
     * 
     * @return 意图类型
     */
    UserIntent getIntentType();
}
package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;

/**
 * NLP引擎接口
 * 用于识别用户在多模态请求中的真实意图
 */
public interface NlpEngine {
    /**
     * 识别用户意图
     * @param prompt 用户输入的提示文本
     * @return 识别出的用户意图
     */
    UserIntent recognizeIntent(String prompt);
}
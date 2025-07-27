package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;

import java.util.Map;
import java.util.HashMap;

/**
 * 意图识别结果
 * 包含识别出的意图和提取的相关参数
 */
public class IntentRecognitionResult {
    
    private final UserIntent intent;
    private final Map<String, String> parameters;
    
    public IntentRecognitionResult(UserIntent intent, Map<String, String> parameters) {
        this.intent = intent;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
    }
    
    public UserIntent getIntent() {
        return intent;
    }
    
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public String getParameter(String key) {
        return parameters.get(key);
    }
    
    public String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }
    
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    public static IntentRecognitionResult of(UserIntent intent) {
        return new IntentRecognitionResult(intent, null);
    }
    
    public static IntentRecognitionResult of(UserIntent intent, Map<String, String> parameters) {
        return new IntentRecognitionResult(intent, parameters);
    }
}
package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 参数提取器管理器
 * 管理参数提取器链，根据输入内容选择合适的提取器
 */
@Component
public class ParameterExtractorManager {
    
    private final List<ParameterExtractor> parameterExtractors;
    
    @Autowired
    public ParameterExtractorManager(List<ParameterExtractor> parameterExtractors) {
        this.parameterExtractors = parameterExtractors;
    }
    
    /**
     * 从用户输入中提取参数
     * @param input 用户输入
     * @return 提取到的参数映射
     */
    public Map<String, String> extractParameters(String input) {
        Map<String, String> parameters = new HashMap<>();
        
        // 使用参数提取器链来提取参数
        for (ParameterExtractor extractor : parameterExtractors) {
            if (extractor.supports(input)) {
                Map<String, String> extracted = extractor.extractParameters(input);
                parameters.putAll(extracted);
            }
        }
        
        return parameters;
    }
    
    /**
     * 根据输入内容获取适用的参数提取器
     * @param input 用户输入
     * @return 适用的参数提取器列表
     */
    public List<ParameterExtractor> getApplicableExtractors(String input) {
        return parameterExtractors.stream()
                .filter(extractor -> extractor.supports(input))
                .toList();
    }
}
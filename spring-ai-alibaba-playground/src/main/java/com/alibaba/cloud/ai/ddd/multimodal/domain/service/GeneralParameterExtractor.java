package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

/**
 * 通用参数提取器
 * 用于提取通用参数
 */
@Component
public class GeneralParameterExtractor implements ParameterExtractor {
    
    @Override
    public Map<String, String> extractParameters(String input) {
        // 通用参数提取器暂时不提取具体参数
        // 可以根据需要扩展，例如提取时间、地点等通用参数
        return new HashMap<>();
    }
    
    @Override
    public boolean supports(String input) {
        // 通用参数提取器支持所有输入
        return true;
    }
}
package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import java.util.Map;

/**
 * 参数提取器接口
 * 定义从用户输入中提取参数的通用接口
 */
public interface ParameterExtractor {
    
    /**
     * 从用户输入中提取参数
     * @param input 用户输入
     * @return 提取到的参数映射
     */
    Map<String, String> extractParameters(String input);
    
    /**
     * 检查此提取器是否适用于给定的输入
     * @param input 用户输入
     * @return 是否适用
     */
    boolean supports(String input);
}
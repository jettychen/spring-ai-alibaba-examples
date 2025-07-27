package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于AI的NLP引擎实现
 * 使用大语言模型来识别用户意图
 */
@Component
public class SpacyNlpEngine implements NlpEngine {

    private final ChatClient chatClient;
    private final ParameterExtractorManager parameterExtractorManager;
    private final IntentPromptTemplate intentPromptTemplate;
    
    public SpacyNlpEngine(@Qualifier("dashscopeChatModel") ChatModel chatModel, 
                         ParameterExtractorManager parameterExtractorManager,
                         IntentPromptTemplate intentPromptTemplate) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.parameterExtractorManager = parameterExtractorManager;
        this.intentPromptTemplate = intentPromptTemplate;
    }

    @Override
    public UserIntent recognizeIntent(String prompt) {
        // 为了保持接口兼容性，仍然提供只返回意图的方法
        IntentRecognitionResult result = recognizeIntentWithParameters(prompt);
        return result.getIntent();
    }
    
    /**
     * 识别用户意图和相关参数
     * @param prompt 用户输入
     * @return 意图识别结果，包含意图和参数
     */
    public IntentRecognitionResult recognizeIntentWithParameters(String prompt) {
        try {
            // 使用AI模型识别意图和参数
            String response = chatClient.prompt()
                .user(intentPromptTemplate.getIntentPrompt().replace("{user_input}", prompt))
                .call()
                .content();
            
            // 解析AI返回的意图和参数
            return parseIntentWithParameters(response.trim(), prompt);
        } catch (Exception e) {
            // 如果AI识别失败，回退到基于规则的识别
            return fallbackRuleBasedRecognitionWithParameters(prompt);
        }
    }
    
    /**
     * 解析AI返回的意图字符串和参数
     * @param response AI返回的响应
     * @param originalPrompt 原始用户输入
     * @return 意图识别结果
     */
    private IntentRecognitionResult parseIntentWithParameters(String response, String originalPrompt) {
        // 检查是否是JSON格式的响应（包含参数）
        if (response.startsWith("{") && response.endsWith("}")) {
            try {
                // 使用更健壮的JSON解析方法
                Map<String, Object> jsonResult = parseJsonResponse(response);
                String intentStr = (String) jsonResult.get("intent");
                @SuppressWarnings("unchecked")
                Map<String, String> parameters = (Map<String, String>) jsonResult.getOrDefault("parameters", new HashMap<>());
                
                // 合并AI识别的参数和通过规则提取的参数
                Map<String, String> ruleBasedParameters = parameterExtractorManager.extractParameters(originalPrompt);
                parameters.putAll(ruleBasedParameters);
                
                UserIntent intent = parseIntent(intentStr);
                return IntentRecognitionResult.of(intent, parameters);
            } catch (Exception e) {
                // JSON解析失败，回退到简单意图解析
                UserIntent intent = parseIntent(response);
                Map<String, String> parameters = parameterExtractorManager.extractParameters(originalPrompt);
                return IntentRecognitionResult.of(intent, parameters);
            }
        } else {
            // 简单意图解析
            UserIntent intent = parseIntent(response);
            Map<String, String> parameters = parameterExtractorManager.extractParameters(originalPrompt);
            return IntentRecognitionResult.of(intent, parameters);
        }
    }
    
    /**
     * 解析JSON响应
     * @param jsonResponse JSON响应字符串
     * @return 解析后的Map对象
     */
    private Map<String, Object> parseJsonResponse(String jsonResponse) {
        Map<String, Object> result = new HashMap<>();
        
        // 提取intent字段
        Pattern intentPattern = Pattern.compile("\"intent\"\\s*:\\s*\"([^\"]+)\"");
        Matcher intentMatcher = intentPattern.matcher(jsonResponse);
        if (intentMatcher.find()) {
            result.put("intent", intentMatcher.group(1));
        }
        
        // 提取parameters对象
        Map<String, String> parameters = new HashMap<>();
        Pattern paramPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher paramMatcher = paramPattern.matcher(jsonResponse);
        
        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2);
            
            // 只添加预定义的参数类型
            if ("bookId".equals(key) || "bookTitle".equals(key) || 
                "studentId".equals(key) || "studentName".equals(key) || 
                "category".equals(key)) {
                parameters.put(key, value);
            }
        }
        
        result.put("parameters", parameters);
        return result;
    }
    
    /**
     * 解析意图字符串
     * @param intentStr 意图字符串
     * @return 对应的UserIntent枚举值
     */
    private UserIntent parseIntent(String intentStr) {
        if (intentStr == null) {
            return UserIntent.GENERAL_PROCESSING;
        }
        
        switch (intentStr.toUpperCase()) {
            case "VIEW_AVAILABLE_BOOKS":
                return UserIntent.VIEW_AVAILABLE_BOOKS;
            case "SEARCH_BOOKS":
                return UserIntent.SEARCH_BOOKS;
            case "BORROW_BOOK_LIST":
                return UserIntent.BORROW_BOOK_LIST;
            case "BORROW_BOOK_ACTION":
                return UserIntent.BORROW_BOOK_ACTION;
            case "RETURN_BOOK":
                return UserIntent.RETURN_BOOK;
            default:
                return UserIntent.GENERAL_PROCESSING;
        }
    }
    
    /**
     * 基于规则的回退识别方法
     * 当AI识别失败时使用
     * @param prompt 用户输入
     * @return 意图识别结果
     */
    private IntentRecognitionResult fallbackRuleBasedRecognitionWithParameters(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        Map<String, String> parameters = parameterExtractorManager.extractParameters(prompt);
        
        // 更细致的借书意图识别
        if ((lowerPrompt.contains("借") || lowerPrompt.contains("借阅")) && 
            !(lowerPrompt.contains("查看") || lowerPrompt.contains("列表") || lowerPrompt.contains("看看"))) {
            return IntentRecognitionResult.of(UserIntent.BORROW_BOOK_ACTION, parameters);
        } else if ((lowerPrompt.contains("借") || lowerPrompt.contains("借阅")) && 
                   (lowerPrompt.contains("查看") || lowerPrompt.contains("列表") || lowerPrompt.contains("看看"))) {
            return IntentRecognitionResult.of(UserIntent.BORROW_BOOK_LIST, parameters);
        } else if (lowerPrompt.contains("还") || lowerPrompt.contains("归还")) {
            return IntentRecognitionResult.of(UserIntent.RETURN_BOOK, parameters);
        } else if (lowerPrompt.contains("查看") || lowerPrompt.contains("所有") || 
                   lowerPrompt.contains("推荐") || lowerPrompt.contains("可以借")) {
            return IntentRecognitionResult.of(UserIntent.VIEW_AVAILABLE_BOOKS, parameters);
        } else {
            return IntentRecognitionResult.of(UserIntent.SEARCH_BOOKS, parameters);
        }
    }
}
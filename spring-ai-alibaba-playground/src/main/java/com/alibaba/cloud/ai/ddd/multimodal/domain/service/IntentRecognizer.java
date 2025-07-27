package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer.UserIntent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Component
public class IntentRecognizer {

    /**
     * 用户意图枚举
     */
    public enum UserIntent {
        VIEW_AVAILABLE_BOOKS,  // 查看可借书籍
        SEARCH_BOOKS,          // 搜索书籍
        BORROW_BOOK_LIST,      // 查看可借书列表（借书的第一步）
        BORROW_BOOK_ACTION,    // 执行借书操作（借书的第二步）
        RETURN_BOOK,           // 归还书籍
        GENERAL_PROCESSING     // 一般处理（非图书相关）
    }

    private final NlpEngine nlpEngine;

    public IntentRecognizer(NlpEngine nlpEngine) {
        this.nlpEngine = nlpEngine;
    }

    /**
     * 识别用户意图
     * @param task 处理任务
     * @return 用户意图
     */
    public UserIntent recognizeIntent(ProcessingTask task) {
        // 优先从任务参数中获取意图
        String intentParam = task.getParameter("intent");
        if (intentParam != null && !intentParam.isEmpty()) {
            try {
                return UserIntent.valueOf(intentParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果参数中的意图值无效，则继续使用NLP引擎识别
            }
        }
        
        // 使用 NLP 引擎识别意图
        String prompt = task.getPrompt().getContent();
        return nlpEngine.recognizeIntent(prompt);
    }
    
    /**
     * 识别用户意图和相关参数
     * @param task 处理任务
     * @return 意图识别结果，包含意图和参数
     */
    public IntentRecognitionResult recognizeIntentWithParameters(ProcessingTask task) {
        // 优先从任务参数中获取意图
        String intentParam = task.getParameter("intent");
        if (intentParam != null && !intentParam.isEmpty()) {
            try {
                UserIntent intent = UserIntent.valueOf(intentParam.toUpperCase());
                // 合并任务参数中的其他参数
                Map<String, String> parameters = new HashMap<>(task.getParameters());
                // 移除intent参数，避免重复
                parameters.remove("intent");
                return IntentRecognitionResult.of(intent, parameters);
            } catch (IllegalArgumentException e) {
                // 如果参数中的意图值无效，则继续使用NLP引擎识别
            }
        }
        
        String prompt = task.getPrompt().getContent();
        
        // 使用 NLP 引擎识别意图和参数
        if (nlpEngine instanceof SpacyNlpEngine) {
            return ((SpacyNlpEngine) nlpEngine).recognizeIntentWithParameters(prompt);
        } else {
            // 如果不是SpacyNlpEngine，则回退到基本意图识别
            UserIntent intent = nlpEngine.recognizeIntent(prompt);
            return IntentRecognitionResult.of(intent);
        }
    }
}
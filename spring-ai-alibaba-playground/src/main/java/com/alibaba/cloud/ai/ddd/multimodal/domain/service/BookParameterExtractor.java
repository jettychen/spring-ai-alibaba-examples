package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

/**
 * 图书相关参数提取器
 * 专门用于从用户输入中提取图书相关的参数
 */
@Component
public class BookParameterExtractor implements ParameterExtractor {
    
    @Override
    public Map<String, String> extractParameters(String input) {
        Map<String, String> parameters = new HashMap<>();
        
        if (input == null || input.isEmpty()) {
            return parameters;
        }
        
        // 提取图书ID
        extractBookId(input, parameters);
        
        // 提取学号
        extractStudentId(input, parameters);
        
        // 提取学生姓名
        extractStudentName(input, parameters);
        
        // 提取图书标题
        extractBookTitle(input, parameters);
        
        // 提取图书类别
        extractCategory(input, parameters);
        
        return parameters;
    }
    
    @Override
    public boolean supports(String input) {
        // 支持包含图书相关关键词的输入
        if (input == null) return false;
        
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("书") || 
               lowerInput.contains("图书") || 
               lowerInput.contains("书籍") ||
               lowerInput.contains("借") ||
               lowerInput.contains("还") ||
               lowerInput.contains("归还") ||
               lowerInput.contains("学号") ||
               lowerInput.contains("姓名") ||
               BOOK_TITLE_PATTERN.matcher(input).find();
    }
    
    // 定义参数提取的模式
    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("(?:(?:图书|书籍|书)[\\s]*ID|ID)[\\s]*(?:[为是:=\\s]*)[\\s]*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("学号[\\s]*(?:[为是:=\\s]*)[\\s]*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STUDENT_NAME_PATTERN = Pattern.compile("姓名[\\s]*(?:[为是:=\\s]*)[\\s]*([^，,。.\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOOK_TITLE_PATTERN = Pattern.compile("[《\"](.+?)[》\"]");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("(?:编程|程序|数学|文学|历史|小说|科幻|传记|心理学|经济|管理|艺术|音乐|体育|地理|天文|化学|物理|生物)(?:类|书籍|书)?");
    
    // 类别映射
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<String, String>() {{
        put("编程", "编程");
        put("程序", "编程");
        put("数学", "数学");
        put("文学", "文学");
        put("历史", "历史");
        put("小说", "文学");
        put("科幻", "文学");
        put("传记", "文学");
        put("心理学", "心理");
        put("经济", "经济");
        put("管理", "管理");
        put("艺术", "艺术");
        put("音乐", "艺术");
        put("体育", "体育");
        put("地理", "地理");
        put("天文", "天文");
        put("化学", "化学");
        put("物理", "物理");
        put("生物", "生物");
    }};
    
    /**
     * 提取图书ID
     */
    private void extractBookId(String input, Map<String, String> parameters) {
        Matcher matcher = BOOK_ID_PATTERN.matcher(input);
        if (matcher.find()) {
            parameters.put("bookId", matcher.group(1));
        }
    }
    
    /**
     * 提取学号
     */
    private void extractStudentId(String input, Map<String, String> parameters) {
        Matcher matcher = STUDENT_ID_PATTERN.matcher(input);
        if (matcher.find()) {
            parameters.put("studentId", matcher.group(1));
        }
    }
    
    /**
     * 提取学生姓名
     */
    private void extractStudentName(String input, Map<String, String> parameters) {
        Matcher matcher = STUDENT_NAME_PATTERN.matcher(input);
        if (matcher.find()) {
            parameters.put("studentName", matcher.group(1).trim());
        }
    }
    
    /**
     * 提取图书标题
     */
    private void extractBookTitle(String input, Map<String, String> parameters) {
        // 匹配书名号《》或引号""中的内容
        Matcher matcher = BOOK_TITLE_PATTERN.matcher(input);
        if (matcher.find()) {
            parameters.put("bookTitle", matcher.group(1).trim());
        }
    }
    
    /**
     * 提取图书类别
     */
    private void extractCategory(String input, Map<String, String> parameters) {
        Matcher matcher = CATEGORY_PATTERN.matcher(input);
        if (matcher.find()) {
            String categoryKey = matcher.group();
            String category = CATEGORY_MAPPING.getOrDefault(categoryKey, categoryKey);
            parameters.put("category", category);
        }
    }
}
package com.alibaba.cloud.ai.ddd.multimodal.domain.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图识别提示模板
 * 管理意图识别的提示模板，支持动态配置
 */
@Component
public class IntentPromptTemplate {
    
    /**
     * 获取意图识别提示模板
     * @return 提示模板字符串
     */
    public String getIntentPrompt() {
        return """
            你是一个智能意图识别系统。请分析用户输入的内容，并识别出用户的意图和相关参数。
            
            可能的意图包括：
            1. VIEW_AVAILABLE_BOOKS: 用户想要查看可借阅的书籍
            2. SEARCH_BOOKS: 用户想要搜索特定的书籍
            3. BORROW_BOOK_LIST: 用户想要查看可借书列表（只是想看看有哪些书可以借）
            4. BORROW_BOOK_ACTION: 用户想要借阅书籍（已经知道要借什么书，准备执行借书操作）
            5. RETURN_BOOK: 用户想要归还书籍
            6. GENERAL_PROCESSING: 其他一般性处理请求
            
            注意区分BORROW_BOOK_LIST和BORROW_BOOK_ACTION：
            - BORROW_BOOK_LIST: 用户只是想查看可借书列表，例如"我想看看有哪些书可以借"、"查看可借书籍"
            - BORROW_BOOK_ACTION: 用户明确表示要借书，例如"我要借书"、"我想借《Java编程思想》"
            
            需要提取的参数包括：
            1. bookId（图书ID）:
               - 格式：数字字符串
               - 示例："图书ID为12345"、"书籍id是67890"
               - 提取规则：提取"图书ID"、"书籍ID"、"书ID"后面的数字
            
            2. bookTitle（图书标题）:
               - 格式：字符串
               - 示例：《Java编程思想》、"深入理解Java虚拟机"
               - 提取规则：提取书名号《》或引号""中的内容
               - 注意：用户可以通过图书标题或图书ID进行借阅操作，两者任选其一即可
            
            3. studentId（学号）:
               - 格式：数字字符串
               - 示例："学号为2021001"、"我的学号2022002"
               - 提取规则：提取"学号"后面的数字
            
            4. studentName（学生姓名）:
               - 格式：字符串
               - 示例："姓名为张三"、"我是李四"
               - 提取规则：提取"姓名"后面的非标点符号内容
            
            5. category（图书类别）:
               - 格式：字符串
               - 示例："编程类"、"数学类"、"文学类"
               - 提取规则：提取明确的类别关键词，如"编程"、"数学"、"文学"、"历史"等
            
            借阅操作说明：
            - 用户可以通过图书ID或图书标题进行借阅
            - 如果用户提供了图书标题，系统将自动查找对应的图书ID
            - 用户必须同时提供学号和姓名才能完成借阅操作
            - 示例输入："我想借《Java核心技术》，学号为2021001，姓名为张三"
            
            如果用户输入中包含参数信息，请以JSON格式返回，格式如下：
            {
              "intent": "意图名称",
              "parameters": {
                "参数名1": "参数值1",
                "参数名2": "参数值2"
              }
            }
            
            参数提取要求：
            1. 准确识别参数类型和值
            2. 保持参数值的原始格式
            3. 如果没有识别到参数，parameters字段应为空对象{}
            4. 不要添加未在用户输入中明确提及的参数
            5. 如果用户同时提供了图书ID和图书标题，优先使用图书ID
            
            如果没有参数，只需返回意图名称。
            
            用户输入：{user_input}
            """;
    }
    
    /**
     * 获取意图列表
     * @return 意图列表
     */
    public List<String> getIntentList() {
        return List.of(
            "VIEW_AVAILABLE_BOOKS",
            "SEARCH_BOOKS", 
            "BORROW_BOOK_LIST",
            "BORROW_BOOK_ACTION",
            "RETURN_BOOK",
            "GENERAL_PROCESSING"
        );
    }
    
    /**
     * 获取意图描述
     * @param intent 意图名称
     * @return 意图描述
     */
    public String getIntentDescription(String intent) {
        switch (intent) {
            case "VIEW_AVAILABLE_BOOKS":
                return "用户想要查看可借阅的书籍";
            case "SEARCH_BOOKS":
                return "用户想要搜索特定的书籍";
            case "BORROW_BOOK_LIST":
                return "用户想要查看可借书列表（只是想看看有哪些书可以借）";
            case "BORROW_BOOK_ACTION":
                return "用户想要借阅书籍（已经知道要借什么书，准备执行借书操作）";
            case "RETURN_BOOK":
                return "用户想要归还书籍";
            case "GENERAL_PROCESSING":
                return "其他一般性处理请求";
            default:
                return "未知意图";
        }
    }
}
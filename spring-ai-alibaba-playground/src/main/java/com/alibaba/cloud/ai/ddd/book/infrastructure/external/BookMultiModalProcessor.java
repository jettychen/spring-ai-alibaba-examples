package com.alibaba.cloud.ai.ddd.book.infrastructure.external;

import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.*;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.IntentRecognizer;
import com.alibaba.cloud.ai.ddd.multimodal.domain.service.ProcessingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 图书管理多模态处理器
 * 专门处理与图书管理相关的多模态请求
 */
@Component
public class BookMultiModalProcessor implements ProcessingEngine {

    private static final Logger logger = LoggerFactory.getLogger(BookMultiModalProcessor.class);
    
    private final BookServiceAdapter bookServiceAdapter;

    public BookMultiModalProcessor(BookServiceAdapter bookServiceAdapter) {
        this.bookServiceAdapter = bookServiceAdapter;
    }

    @Override
    public boolean supports(ProcessingTask task) {
        // 支持文本到文本的图书管理请求
        return task != null && 
               task.getInputModality() == ModalityType.TEXT && 
               task.getOutputModality() == ModalityType.TEXT;
    }

    // 添加一个新的处理方法，专门用于根据意图处理任务
    public Mono<ProcessingResult> processWithIntent(ProcessingTask task, IntentRecognizer.UserIntent intent) {
        try {
            switch (intent) {
                case BORROW_BOOK_LIST:
                    // 查看可借书列表意图：直接显示可借书籍列表
                    return handleViewAvailableBooks(task)
                        .map(result -> {
                            String content = result.getContent() + "\n\n以上是可借阅的书籍列表。如果您想借阅某本书，请告诉我书名或图书ID，以及您的学号和姓名。";
                            return ProcessingResult.textResultWithMetadata(
                                content,
                                result.getConfidence(),
                                result.getMetadata()
                            );
                        });
                case BORROW_BOOK_ACTION:
                    // 执行借书操作意图：处理借书请求
                    return handleBorrowRequest(task, task.getPrompt().getContent());
                case RETURN_BOOK:
                    return handleReturnRequest(task, task.getPrompt().getContent());
                case VIEW_AVAILABLE_BOOKS:
                    return handleViewAvailableBooks(task);
                case SEARCH_BOOKS:
                    return handleSearchRequest(task, task.getPrompt().getContent());
                case GENERAL_PROCESSING:
                default:
                    // 默认处理：根据文本内容判断操作（向后兼容）
                    String prompt = task.getPrompt().getContent();
                    if (isBorrowRequest(prompt)) {
                        return handleBorrowRequest(task, prompt);
                    } else if (isReturnRequest(prompt)) {
                        return handleReturnRequest(task, prompt);
                    } else if (isViewAvailableRequest(prompt)) {
                        return handleViewAvailableBooks(task);
                    } else {
                        // 默认为搜索请求
                        return handleSearchRequest(task, prompt);
                    }
            }
        } catch (Exception e) {
            logger.error("Error processing book management request", e);
            return Mono.just(ProcessingResult.textResult(
                    "处理失败: " + e.getMessage(), 
                    0.0
            ));
        }
    }

    @Override
    public Mono<ProcessingResult> process(ProcessingTask task) {
        // 从任务参数中获取意图，如果不存在则使用回退逻辑
        String intentParam = task.getParameter("intent");
        IntentRecognizer.UserIntent intent;
        
        if (intentParam != null && !intentParam.isEmpty()) {
            // 如果参数中包含意图，则直接使用
            intent = parseIntentFromString(intentParam);
        } else {
            // 否则使用回退逻辑，根据任务内容动态判断
            String prompt = task.getPrompt().getContent();
            intent = determineIntentFromPrompt(prompt);
        }
        
        return processWithIntent(task, intent);
    }
    
    /**
     * 从字符串解析意图
     * @param intentStr 意图字符串
     * @return 对应的意图枚举
     */
    private IntentRecognizer.UserIntent parseIntentFromString(String intentStr) {
        try {
            return IntentRecognizer.UserIntent.valueOf(intentStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 如果解析失败，回退到根据提示内容判断
            return IntentRecognizer.UserIntent.GENERAL_PROCESSING;
        }
    }
    
    /**
     * 根据提示内容动态判断意图
     * 用于向后兼容旧的处理方式
     * @param prompt 提示内容
     * @return 判断出的意图
     */
    private IntentRecognizer.UserIntent determineIntentFromPrompt(String prompt) {
        if (isBorrowRequest(prompt)) {
            return IntentRecognizer.UserIntent.BORROW_BOOK_ACTION;
        } else if (isReturnRequest(prompt)) {
            return IntentRecognizer.UserIntent.RETURN_BOOK;
        } else if (isViewAvailableRequest(prompt)) {
            return IntentRecognizer.UserIntent.VIEW_AVAILABLE_BOOKS;
        } else {
            return IntentRecognizer.UserIntent.SEARCH_BOOKS;
        }
    }
    
    /**
     * 处理带有意图的任务
     * @param task 处理任务
     * @param intent 识别出的意图
     * @return 处理结果
     */
    public Mono<ProcessingResult> process(ProcessingTask task, IntentRecognizer.UserIntent intent) {
        return processWithIntent(task, intent);
    }

    @Override
    public Flux<ProcessingResult> processStream(ProcessingTask task) {
        // 图书管理操作通常不需要流式处理，直接调用同步方法
        return process(task).flux();
    }

    @Override
    public int getPriority() {
        return 10; // 设置较高优先级
    }

    @Override
    public String getEngineName() {
        return "BookManagementProcessor";
    }

    @Override
    public long estimateProcessingTime(ProcessingTask task) {
        return 1500; // 估计1.5秒处理时间
    }

    @Override
    public boolean isHealthy() {
        return true; // 假设始终健康
    }

    /**
     * 判断是否为借书请求
     */
    private boolean isBorrowRequest(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        
        // 动态提取关键词，而不是硬编码
        List<String> borrowKeywords = Arrays.asList("借", "借阅", "借书", "我要借", "想借", "需要借");
        List<String> bookRelatedKeywords = Arrays.asList("书", "图书", "书籍");
        
        boolean hasBorrowKeyword = borrowKeywords.stream().anyMatch(lowerPrompt::contains);
        boolean hasBookKeyword = bookRelatedKeywords.stream().anyMatch(lowerPrompt::contains);
        
        // 如果同时包含借阅类关键词和书籍相关关键词，则认为是借书请求
        // 或者如果包含特定的组合关键词
        return hasBorrowKeyword && hasBookKeyword || 
               lowerPrompt.contains("借阅书籍") || 
               lowerPrompt.contains("图书借阅");
    }

    /**
     * 判断是否为还书请求
     */
    private boolean isReturnRequest(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        
        // 动态提取关键词，而不是硬编码
        List<String> returnKeywords = Arrays.asList("还", "归还", "还书", "我要还", "想还", "需要还");
        List<String> bookRelatedKeywords = Arrays.asList("书", "图书", "书籍");
        
        boolean hasReturnKeyword = returnKeywords.stream().anyMatch(lowerPrompt::contains);
        boolean hasBookKeyword = bookRelatedKeywords.stream().anyMatch(lowerPrompt::contains);
        
        // 如果同时包含归还类关键词和书籍相关关键词，则认为是还书请求
        // 或者如果包含特定的组合关键词
        return hasReturnKeyword && hasBookKeyword || 
               lowerPrompt.contains("归还书籍") || 
               lowerPrompt.contains("图书归还") ||
               lowerPrompt.contains("还书");
    }

    /**
     * 判断是否为查看可借书籍请求
     */
    private boolean isViewAvailableRequest(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        
        // 动态提取关键词，而不是硬编码
        List<String> viewKeywords = Arrays.asList("查看", "所有", "推荐", "可以借", "可借", "浏览", "看看", "有哪些", "有什么");
        List<String> bookRelatedKeywords = Arrays.asList("书", "图书", "书籍");
        
        boolean hasViewKeyword = viewKeywords.stream().anyMatch(lowerPrompt::contains);
        boolean hasBookKeyword = bookRelatedKeywords.stream().anyMatch(lowerPrompt::contains);
        
        // 如果同时包含查看类关键词和书籍相关关键词，则认为是查看可借书籍请求
        // 或者如果包含特定的组合关键词
        return hasViewKeyword && hasBookKeyword || 
               lowerPrompt.contains("可借书籍") || 
               lowerPrompt.contains("借阅列表") ||
               lowerPrompt.contains("图书列表");
    }

    /**
     * 处理借书请求
     */
    private Mono<ProcessingResult> handleBorrowRequest(ProcessingTask task, String prompt) {
        // 优先从任务参数中获取信息，如果没有则从提示中提取
        String bookId = task.getParameter("bookId");
        if (bookId == null) {
            bookId = extractBookIdFromPrompt(prompt);
        }
        
        // 如果通过书名获取图书ID
        String bookTitle = task.getParameter("bookTitle");
        if (bookTitle == null) {
            bookTitle = extractBookTitleFromPrompt(prompt);
        }
        
        // 如果有书名但没有书ID，则通过书名查找书ID
        if (bookId == null && bookTitle != null) {
            bookId = bookServiceAdapter.getBookIdByTitle(bookTitle);
        }
        
        String studentId = task.getParameter("studentId");
        if (studentId == null) {
            studentId = extractStudentIdFromPrompt(prompt);
        }
        
        String studentName = task.getParameter("studentName");
        if (studentName == null) {
            studentName = extractStudentNameFromPrompt(prompt);
        }
        
        // 如果没有提供完整信息，则显示可借阅的书籍列表
        if (bookId == null || studentId == null || studentName == null) {
            // 显示可借阅书籍列表，引导用户选择
            return handleViewAvailableBooks(task)
                .map(result -> {
                    String content = result.getContent() + "\n\n请从以上可借阅书籍中选择一本，并提供图书名称或ID、学号和姓名以完成借阅。";
                    return ProcessingResult.textResultWithMetadata(
                        content,
                        result.getConfidence(),
                        result.getMetadata()
                    );
                });
        }
        
        // 如果提供了完整信息，则执行借阅操作
        try {
            BorrowRecord record = bookServiceAdapter.borrowBook(bookId, studentId, studentName);
            String content = String.format(
                    "借阅成功！\n图书名称: %s\n图书ID: %s\n学号: %s\n姓名: %s\n借阅时间: %s", 
                    bookServiceAdapter.getBookById(bookId).getTitle(),
                    bookId, studentId, studentName, 
                    record.getBorrowTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            ProcessingResult result = ProcessingResult.textResultWithMetadata(
                    content, 
                    0.95, // 置信度
                    Map.of(
                        "taskId", task.getId().toString(),
                        "operation", "borrow",
                        "bookId", bookId,
                        "studentId", studentId
                    )
            );
            
            return Mono.just(result);
        } catch (Exception e) {
            return Mono.just(ProcessingResult.textResult(
                    "借阅失败，请检查图书名称或ID或确认图书是否可借", 
                    0.0
            ));
        }
    }

    /**
     * 处理还书请求
     */
    private Mono<ProcessingResult> handleReturnRequest(ProcessingTask task, String prompt) {
        // 优先从任务参数中获取信息，如果没有则从提示中提取
        String bookId = task.getParameter("bookId");
        if (bookId == null) {
            bookId = extractBookIdFromPrompt(prompt);
        }
        
        // 如果通过书名获取图书ID
        String bookTitle = task.getParameter("bookTitle");
        if (bookTitle == null) {
            bookTitle = extractBookTitleFromPrompt(prompt);
        }
        
        // 如果有书名但没有书ID，则通过书名查找书ID
        if (bookId == null && bookTitle != null) {
            bookId = bookServiceAdapter.getBookIdByTitle(bookTitle);
        }
        
        String studentId = task.getParameter("studentId");
        if (studentId == null) {
            studentId = extractStudentIdFromPrompt(prompt);
        }
        
        if (bookId == null || studentId == null) {
            return Mono.just(ProcessingResult.textResult(
                    "请提供完整的归还信息，包括图书名称或ID和学号", 
                    0.8
            ));
        }
        
        try {
            bookServiceAdapter.returnBook(bookId, studentId);
            ProcessingResult result = ProcessingResult.textResultWithMetadata(
                    String.format("图书 %s 归还成功", bookServiceAdapter.getBookById(bookId).getTitle()),
                    0.95,
                    Map.of(
                        "taskId", task.getId().toString(),
                        "operation", "return",
                        "bookId", bookId,
                        "studentId", studentId
                    )
            );
            return Mono.just(result);
        } catch (Exception e) {
            return Mono.just(ProcessingResult.textResult(
                    "归还失败，请检查图书名称或ID或确认图书状态", 
                    0.0
            ));
        }
    }

    /**
     * 处理查看可借书籍请求
     */
    private Mono<ProcessingResult> handleViewAvailableBooks(ProcessingTask task) {
        try {
            List<Book> books = bookServiceAdapter.getAllBooks();
            // 筛选出可借阅的书籍
            List<Book> availableBooks = books.stream()
                    .filter(Book::isAvailable)
                    .collect(Collectors.toList());
            
            String content = formatAvailableBooks(availableBooks);
            
            ProcessingResult result = ProcessingResult.textResultWithMetadata(
                    content, 
                    0.95, // 置信度
                    Map.of(
                        "taskId", task.getId().toString(),
                        "operation", "view_available",
                        "bookCount", availableBooks.size()
                    )
            );
            
            return Mono.just(result);
        } catch (Exception e) {
            return Mono.just(ProcessingResult.textResult(
                    "获取可借阅图书列表失败", 
                    0.0
            ));
        }
    }

    /**
     * 处理搜索请求
     */
    private Mono<ProcessingResult> handleSearchRequest(ProcessingTask task, String prompt) {
        try {
            List<Book> books;
            // 优先从任务参数中获取类别信息
            String category = task.getParameter("category");
            if (category != null) {
                // 按类别搜索
                books = bookServiceAdapter.searchBooksByCategory(category);
            } else if (prompt.toLowerCase().contains("类别") || prompt.toLowerCase().contains("分类")) {
                // 从提示中提取类别信息
                category = extractCategoryFromPrompt(prompt);
                books = bookServiceAdapter.searchBooksByCategory(category);
            } else {
                // 按文本搜索
                books = bookServiceAdapter.searchBooksByText(prompt);
            }
            
            String content = formatSearchResults(books);
            
            ProcessingResult result = ProcessingResult.textResultWithMetadata(
                    content, 
                    0.9, // 置信度
                    Map.of(
                        "taskId", task.getId().toString(),
                        "operation", "search",
                        "bookCount", books.size()
                    )
            );
            
            return Mono.just(result);
        } catch (Exception e) {
            return Mono.just(ProcessingResult.textResult(
                    "搜索图书失败", 
                    0.0
            ));
        }
    }

    /**
     * 从提示中提取图书ID
     */
    private String extractBookIdFromPrompt(String prompt) {
        // 简单实现，实际应用中可以使用更复杂的NLP技术
        if (prompt.contains("ID为")) {
            return prompt.replaceAll(".*ID为(\\d+).*", "$1");
        } else if (prompt.contains("id为")) {
            return prompt.replaceAll(".*id为(\\d+).*", "$1");
        }
        return null;
    }

    /**
     * 从提示中提取学生ID
     */
    private String extractStudentIdFromPrompt(String prompt) {
        // 简单实现，实际应用中可以使用更复杂的NLP技术
        if (prompt.contains("学号为")) {
            return prompt.replaceAll(".*学号为(\\d+).*", "$1");
        } else if (prompt.contains("学号")) {
            return prompt.replaceAll(".*学号(\\d+).*", "$1");
        }
        return null;
    }

    /**
     * 从提示中提取学生姓名
     */
    private String extractStudentNameFromPrompt(String prompt) {
        // 简单实现，实际应用中可以使用更复杂的NLP技术
        if (prompt.contains("姓名为")) {
            return prompt.replaceAll(".*姓名为([^，,。.]+).*", "$1").trim();
        } else if (prompt.contains("姓名")) {
            return prompt.replaceAll(".*姓名([^，,。.]+).*", "$1").trim();
        }
        return null;
    }
    
    /**
     * 从提示中提取图书标题
     */
    private String extractBookTitleFromPrompt(String prompt) {
        // 简单实现，实际应用中可以使用更复杂的NLP技术
        // 匹配书名号《》或引号""中的内容
        Pattern pattern = Pattern.compile("[《\"](.+?)[》\"]");
        Matcher matcher = pattern.matcher(prompt);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 从提示中提取类别信息
     */
    private String extractCategoryFromPrompt(String prompt) {
        // 简单实现，实际应用中可以使用更复杂的NLP技术
        if (prompt.contains("编程")) {
            return "编程";
        } else if (prompt.contains("数学")) {
            return "数学";
        } else {
            return prompt.replaceAll(".*类别(.*)", "$1").trim();
        }
    }

    /**
     * 格式化可借书籍结果
     */
    private String formatAvailableBooks(List<Book> books) {
        if (books.isEmpty()) {
            return "当前没有可借阅的图书。";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("当前可借阅的图书有 ").append(books.size()).append(" 本:\n\n");
        
        for (int i = 0; i < Math.min(books.size(), 10); i++) { // 限制显示前10本
            Book book = books.get(i);
            sb.append(i + 1).append(". 《").append(book.getTitle()).append("》\n");
            sb.append("   作者: ").append(book.getAuthor()).append("\n");
            sb.append("   类别: ").append(book.getCategory()).append("\n");
            sb.append("   图书ID: ").append(book.getId().getId()).append("\n\n");
        }
        
        if (books.size() > 10) {
            sb.append("... 还有 ").append(books.size() - 10).append(" 本图书，可通过搜索功能查找更多。\n\n");
        }
        
        sb.append("如需借阅，请告诉我书名或图书ID，以及您的学号和姓名。");
        return sb.toString();
    }

    /**
     * 格式化搜索结果
     */
    private String formatSearchResults(List<Book> books) {
        if (books.isEmpty()) {
            return "未找到相关图书。";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(books.size()).append(" 本相关图书:\n\n");
        
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            sb.append(i + 1).append(". 《").append(book.getTitle()).append("》\n");
            sb.append("   作者: ").append(book.getAuthor()).append("\n");
            sb.append("   类别: ").append(book.getCategory()).append("\n");
            sb.append("   简介: ").append(book.getDescription()).append("\n");
            sb.append("   状态: ").append(book.isAvailable() ? "可借阅" : "已借出").append("\n");
            sb.append("   图书ID: ").append(book.getId().getId()).append("\n\n");
        }
        
        return sb.toString();
    }
}
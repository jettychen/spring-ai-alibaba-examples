package com.alibaba.cloud.ai.ddd.book.infrastructure.external;

import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BookId;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecordId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 图书服务适配器
 * 提供图书管理功能的实现
 */
@Component
public class BookServiceAdapter {
    
    // 模拟数据库存储
    private final Map<BookId, Book> books = new ConcurrentHashMap<>();
    private final Map<BorrowRecordId, BorrowRecord> borrowRecords = new ConcurrentHashMap<>();
    
    public BookServiceAdapter() {
        // 初始化一些示例数据
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        // 添加一些示例图书
        Book book1 = new Book(
                BookId.of("1"),
                "Java核心技术",
                "Cay S. Horstmann",
                "9787111547426",
                "《Java核心技术》是Java领域最有影响力和价值的著作之一",
                "编程",
                List.of("Java", "编程", "计算机科学"),
                "/covers/java-core.jpg",
                true
        );
        books.put(book1.getId(), book1);
        
        Book book2 = new Book(
                BookId.of("2"),
                "Spring实战",
                "Craig Walls",
                "9787115422617",
                "《Spring实战》通过大量示例讲解了Spring框架的使用",
                "编程",
                List.of("Spring", "Java", "框架"),
                "/covers/spring-in-action.jpg",
                true
        );
        books.put(book2.getId(), book2);
        
        Book book3 = new Book(
                BookId.of("3"),
                "设计模式",
                "Gang of Four",
                "9787111547427",
                "《设计模式》是软件开发领域经典著作，介绍了23种设计模式",
                "编程",
                List.of("设计模式", "面向对象", "软件工程"),
                "/covers/design-patterns.jpg",
                false
        );
        books.put(book3.getId(), book3);
        
        Book book4 = new Book(
                BookId.of("4"),
                "算法导论",
                "Thomas H. Cormen",
                "9787111547428",
                "《算法导论》提供了对算法和数据结构的深入理解",
                "数学",
                List.of("算法", "数据结构", "数学"),
                "/covers/introduction-to-algorithms.jpg",
                true
        );
        books.put(book4.getId(), book4);
    }
    
    /**
     * 根据文本搜索图书
     * @param query 搜索关键词
     * @return 匹配的图书列表
     */
    public List<Book> searchBooksByText(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>(books.values());
        }
        
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                               book.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                               book.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                               book.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                               book.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据类别搜索图书
     * @param category 类别
     * @return 匹配的图书列表
     */
    public List<Book> searchBooksByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return new ArrayList<>();
        }
        
        return books.values().stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category) ||
                               book.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }
    
    /**
     * 借阅图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @param studentName 学生姓名
     * @return 借阅记录
     */
    public BorrowRecord borrowBook(String bookId, String studentId, String studentName) {
        Book book = books.get(BookId.of(bookId));
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }

        if (!book.isAvailable()) {
            throw new IllegalStateException("图书已被借出");
        }

        // 更新图书状态
        book.setAvailable(false);
        books.put(book.getId(), book);

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord(
                BorrowRecordId.generate(), 
                BookId.of(bookId), 
                studentId, 
                studentName);

        borrowRecords.put(record.getId(), record);
        return record;
    }
    
    /**
     * 归还图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     */
    public void returnBook(String bookId, String studentId) {
        Book book = books.get(BookId.of(bookId));
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        
        // 更新图书状态
        book.setAvailable(true);
        books.put(book.getId(), book);
        
        // 更新借阅记录
        borrowRecords.values().stream()
                .filter(record -> record.getBookId().getId().equals(bookId) && 
                                record.getStudentId().equals(studentId) && 
                                !record.isReturned())
                .findFirst()
                .ifPresent(record -> {
                    record.returnBook();
                    borrowRecords.put(record.getId(), record);
                });
    }
    
    /**
     * 获取所有图书
     * @return 图书列表
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }
    
    /**
     * 根据ID获取图书
     * @param bookId 图书ID
     * @return 图书
     */
    public Book getBookById(String bookId) {
        return books.get(BookId.of(bookId));
    }
    
    /**
     * 根据书名查找图书
     * @param bookTitle 图书标题
     * @return 图书ID，如果未找到则返回null
     */
    public String getBookIdByTitle(String bookTitle) {
        if (bookTitle == null || bookTitle.isEmpty()) {
            return null;
        }
        
        return books.values().stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(bookTitle) ||
                               book.getTitle().contains(bookTitle) ||
                               bookTitle.contains(book.getTitle()))
                .findFirst()
                .map(book -> book.getId().getId())
                .orElse(null);
    }
}
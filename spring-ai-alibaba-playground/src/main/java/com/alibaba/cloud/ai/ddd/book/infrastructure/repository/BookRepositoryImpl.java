package com.alibaba.cloud.ai.ddd.book.infrastructure.repository;

import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BookId;
import com.alibaba.cloud.ai.ddd.book.domain.repository.BookRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 图书仓储实现
 */
@Repository
public class BookRepositoryImpl implements BookRepository {
    
    // 模拟数据库存储
    private final Map<BookId, Book> books = new ConcurrentHashMap<>();
    
    public BookRepositoryImpl() {
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
    
    @Override
    public void save(Book book) {
        books.put(book.getId(), book);
    }
    
    @Override
    public Optional<Book> findById(BookId bookId) {
        return Optional.ofNullable(books.get(bookId));
    }
    
    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }
    
    @Override
    public List<Book> findByTitleContaining(String title) {
        if (title == null || title.isEmpty()) {
            return new ArrayList<>();
        }
        
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Book> findByAuthorContaining(String author) {
        if (author == null || author.isEmpty()) {
            return new ArrayList<>();
        }
        
        return books.values().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Book> findByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return new ArrayList<>();
        }
        
        return books.values().stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category) ||
                               book.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Book> findAvailableBooks() {
        return books.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }
}
package com.alibaba.cloud.ai.ddd.book.application.service;

import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.book.domain.service.BookDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 图书应用服务
 * 协调用例和领域服务
 */
@Service
public class BookApplicationService {
    
    private final BookDomainService bookDomainService;
    
    public BookApplicationService(BookDomainService bookDomainService) {
        this.bookDomainService = bookDomainService;
    }
    
    /**
     * 借阅图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @param studentName 学生姓名
     * @return 借阅记录
     */
    public Mono<BorrowRecord> borrowBook(String bookId, String studentId, String studentName) {
        return Mono.fromCallable(() -> bookDomainService.borrowBook(bookId, studentId, studentName));
    }
    
    /**
     * 归还图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @return 空Mono
     */
    public Mono<Void> returnBook(String bookId, String studentId) {
        return Mono.fromRunnable(() -> bookDomainService.returnBook(bookId, studentId));
    }
    
    /**
     * 搜索图书
     * @param keyword 搜索关键词
     * @return 图书列表
     */
    public Mono<List<Book>> searchBooks(String keyword) {
        return Mono.fromCallable(() -> bookDomainService.searchBooks(keyword));
    }
    
    /**
     * 根据类别搜索图书
     * @param category 类别
     * @return 图书列表
     */
    public Mono<List<Book>> searchBooksByCategory(String category) {
        return Mono.fromCallable(() -> bookDomainService.searchBooksByCategory(category));
    }
    
    /**
     * 查看可借阅图书
     * @return 可借阅图书列表
     */
    public Mono<List<Book>> viewAvailableBooks() {
        return Mono.fromCallable(bookDomainService::viewAvailableBooks);
    }
    
    /**
     * 根据ID获取图书
     * @param bookId 图书ID
     * @return 图书
     */
    public Mono<Book> getBookById(String bookId) {
        return Mono.fromCallable(() -> bookDomainService.getBookById(bookId));
    }
}
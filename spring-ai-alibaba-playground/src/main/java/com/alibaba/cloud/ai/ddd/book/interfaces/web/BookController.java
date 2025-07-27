package com.alibaba.cloud.ai.ddd.book.interfaces.web;

import com.alibaba.cloud.ai.ddd.book.application.service.BookApplicationService;
import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 图书控制器
 * 提供REST API接口
 */
@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    
    private final BookApplicationService bookApplicationService;
    
    public BookController(BookApplicationService bookApplicationService) {
        this.bookApplicationService = bookApplicationService;
    }
    
    /**
     * 搜索图书
     * @param query 搜索关键词
     * @return 图书列表
     */
    @GetMapping("/search")
    public Mono<ResponseEntity<List<Book>>> searchBooks(@RequestParam(required = false, defaultValue = "") String query) {
        return bookApplicationService.searchBooks(query)
                .map(books -> ResponseEntity.ok(books))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * 根据类别搜索图书
     * @param category 类别
     * @return 图书列表
     */
    @GetMapping("/category/{category}")
    public Mono<ResponseEntity<List<Book>>> searchBooksByCategory(@PathVariable String category) {
        return bookApplicationService.searchBooksByCategory(category)
                .map(books -> ResponseEntity.ok(books))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * 查看可借阅图书
     * @return 可借阅图书列表
     */
    @GetMapping("/available")
    public Mono<ResponseEntity<List<Book>>> viewAvailableBooks() {
        return bookApplicationService.viewAvailableBooks()
                .map(books -> ResponseEntity.ok(books))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * 借阅图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @param studentName 学生姓名
     * @return 借阅记录
     */
    @PostMapping("/{bookId}/borrow")
    public Mono<ResponseEntity<BorrowRecord>> borrowBook(
            @PathVariable String bookId,
            @RequestParam String studentId,
            @RequestParam String studentName) {
        return bookApplicationService.borrowBook(bookId, studentId, studentName)
                .map(record -> ResponseEntity.ok(record))
                .onErrorResume(DomainException.class, e -> 
                    Mono.just(ResponseEntity.badRequest().build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * 归还图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @return 空响应
     */
    @PostMapping("/{bookId}/return")
    public Mono<ResponseEntity<Void>> returnBook(
            @PathVariable String bookId,
            @RequestParam String studentId) {
        return bookApplicationService.returnBook(bookId, studentId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(DomainException.class, e -> 
                    Mono.just(ResponseEntity.badRequest().build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * 根据ID获取图书
     * @param bookId 图书ID
     * @return 图书
     */
    @GetMapping("/{bookId}")
    public Mono<ResponseEntity<Book>> getBookById(@PathVariable String bookId) {
        return bookApplicationService.getBookById(bookId)
                .map(book -> ResponseEntity.ok(book))
                .onErrorResume(DomainException.class, e -> 
                    Mono.just(ResponseEntity.notFound().build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
package com.alibaba.cloud.ai.ddd.book.domain.service;

import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BookId;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecordId;
import com.alibaba.cloud.ai.ddd.book.domain.repository.BookRepository;
import com.alibaba.cloud.ai.ddd.book.domain.repository.BorrowRecordRepository;
import com.alibaba.cloud.ai.ddd.shared.exception.DomainException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书领域服务
 * 处理图书相关的复杂业务逻辑
 */
@Service
public class BookDomainService {
    
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    
    public BookDomainService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }
    
    /**
     * 借阅图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @param studentName 学生姓名
     * @return 借阅记录
     */
    public BorrowRecord borrowBook(String bookId, String studentId, String studentName) {
        // 查找图书
        Book book = bookRepository.findById(BookId.of(bookId))
                .orElseThrow(() -> new DomainException("BOOK_NOT_FOUND", "图书不存在"));
        
        // 检查图书是否可借
        if (!book.isAvailable()) {
            throw new DomainException("BOOK_NOT_AVAILABLE", "图书已被借出");
        }
        
        // 更新图书状态为已借出
        book.setAvailable(false);
        bookRepository.save(book);
        
        // 创建借阅记录
        BorrowRecord borrowRecord = new BorrowRecord(
                BorrowRecordId.generate(),
                book.getId(),
                studentId,
                studentName
        );
        
        // 保存借阅记录
        borrowRecordRepository.save(borrowRecord);
        
        return borrowRecord;
    }
    
    /**
     * 归还图书
     * @param bookId 图书ID
     * @param studentId 学生ID
     */
    public void returnBook(String bookId, String studentId) {
        // 查找图书
        Book book = bookRepository.findById(BookId.of(bookId))
                .orElseThrow(() -> new DomainException("BOOK_NOT_FOUND", "图书不存在"));
        
        // 查找未归还的借阅记录
        BorrowRecord borrowRecord = borrowRecordRepository
                .findByBookIdAndStudentIdAndNotReturned(book.getId(), studentId)
                .orElseThrow(() -> new DomainException("BORROW_RECORD_NOT_FOUND", "未找到借阅记录"));
        
        // 更新借阅记录状态
        borrowRecord.returnBook();
        borrowRecordRepository.save(borrowRecord);
        
        // 更新图书状态为可借
        book.setAvailable(true);
        bookRepository.save(book);
    }
    
    /**
     * 搜索图书
     * @param keyword 搜索关键词
     * @return 图书列表
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return bookRepository.findAll();
        }
        
        // 综合搜索：标题、作者、描述、类别、标签
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                               book.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                               book.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                               book.getCategory().toLowerCase().contains(keyword.toLowerCase()) ||
                               book.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(keyword.toLowerCase())))
                .toList();
    }
    
    /**
     * 根据类别搜索图书
     * @param category 类别
     * @return 图书列表
     */
    public List<Book> searchBooksByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return List.of();
        }
        
        return bookRepository.findByCategory(category);
    }
    
    /**
     * 查看可借阅图书
     * @return 可借阅图书列表
     */
    public List<Book> viewAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    /**
     * 根据ID获取图书
     * @param bookId 图书ID
     * @return 图书
     */
    public Book getBookById(String bookId) {
        return bookRepository.findById(BookId.of(bookId))
                .orElseThrow(() -> new DomainException("BOOK_NOT_FOUND", "图书不存在"));
    }
}
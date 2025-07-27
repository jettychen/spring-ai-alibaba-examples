package com.alibaba.cloud.ai.ddd.book;

import com.alibaba.cloud.ai.ddd.book.application.service.BookApplicationService;
import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.book.domain.service.BookDomainService;
import com.alibaba.cloud.ai.ddd.book.infrastructure.repository.BookRepositoryImpl;
import com.alibaba.cloud.ai.ddd.book.infrastructure.repository.BorrowRecordRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookModuleIntegrationTest {

    private BookApplicationService bookApplicationService;
    private BookDomainService bookDomainService;

    @BeforeEach
    void setUp() {
        BookRepositoryImpl bookRepository = new BookRepositoryImpl();
        BorrowRecordRepositoryImpl borrowRecordRepository = new BorrowRecordRepositoryImpl();
        bookDomainService = new BookDomainService(bookRepository, borrowRecordRepository);
        bookApplicationService = new BookApplicationService(bookDomainService);
    }

    @Test
    void testSearchBooks() {
        List<Book> books = bookApplicationService.searchBooks("Java").block();

        assertNotNull(books);
        assertFalse(books.isEmpty());
        assertTrue(books.stream().anyMatch(book -> book.getTitle().contains("Java")));
    }

    @Test
    void testViewAvailableBooks() {
        List<Book> books = bookApplicationService.viewAvailableBooks().block();

        assertNotNull(books);
        assertFalse(books.isEmpty());
        assertTrue(books.stream().allMatch(Book::isAvailable));
    }

    @Test
    void testBorrowAndReturnBook() {
        // 借书
        BorrowRecord record = bookApplicationService.borrowBook("1", "student001", "张三").block();

        assertNotNull(record);
        assertEquals("1", record.getBookId().getId());
        assertEquals("student001", record.getStudentId());
        assertEquals("张三", record.getStudentName());

        // 还书
        bookApplicationService.returnBook("1", "student001").block();
    }

    @Test
    void testGetBookById() {
        Book book = bookApplicationService.getBookById("1").block();

        assertNotNull(book);
        assertEquals("1", book.getId().getId());
        assertEquals("Java核心技术", book.getTitle());
    }
}
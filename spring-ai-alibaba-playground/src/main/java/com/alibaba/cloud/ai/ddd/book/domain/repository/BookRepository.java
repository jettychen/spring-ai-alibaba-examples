package com.alibaba.cloud.ai.ddd.book.domain.repository;

import com.alibaba.cloud.ai.ddd.book.domain.model.Book;
import com.alibaba.cloud.ai.ddd.book.domain.model.BookId;

import java.util.List;
import java.util.Optional;

/**
 * 图书仓储接口
 */
public interface BookRepository {
    
    /**
     * 保存图书
     * @param book 图书实体
     */
    void save(Book book);
    
    /**
     * 根据ID查找图书
     * @param bookId 图书ID
     * @return 图书实体
     */
    Optional<Book> findById(BookId bookId);
    
    /**
     * 查找所有图书
     * @return 图书列表
     */
    List<Book> findAll();
    
    /**
     * 根据标题搜索图书
     * @param title 标题关键词
     * @return 图书列表
     */
    List<Book> findByTitleContaining(String title);
    
    /**
     * 根据作者搜索图书
     * @param author 作者关键词
     * @return 图书列表
     */
    List<Book> findByAuthorContaining(String author);
    
    /**
     * 根据类别搜索图书
     * @param category 类别
     * @return 图书列表
     */
    List<Book> findByCategory(String category);
    
    /**
     * 查找可借阅的图书
     * @return 图书列表
     */
    List<Book> findAvailableBooks();
}
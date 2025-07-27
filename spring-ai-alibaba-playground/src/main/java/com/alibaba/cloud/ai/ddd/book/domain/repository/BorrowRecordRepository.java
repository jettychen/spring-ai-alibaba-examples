package com.alibaba.cloud.ai.ddd.book.domain.repository;

import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecordId;
import com.alibaba.cloud.ai.ddd.book.domain.model.BookId;

import java.util.List;
import java.util.Optional;

/**
 * 借阅记录仓储接口
 */
public interface BorrowRecordRepository {
    
    /**
     * 保存借阅记录
     * @param borrowRecord 借阅记录实体
     */
    void save(BorrowRecord borrowRecord);
    
    /**
     * 根据ID查找借阅记录
     * @param borrowRecordId 借阅记录ID
     * @return 借阅记录实体
     */
    Optional<BorrowRecord> findById(BorrowRecordId borrowRecordId);
    
    /**
     * 根据图书ID和学生ID查找未归还的借阅记录
     * @param bookId 图书ID
     * @param studentId 学生ID
     * @return 借阅记录实体
     */
    Optional<BorrowRecord> findByBookIdAndStudentIdAndNotReturned(BookId bookId, String studentId);
    
    /**
     * 根据学生ID查找借阅记录
     * @param studentId 学生ID
     * @return 借阅记录列表
     */
    List<BorrowRecord> findByStudentId(String studentId);
    
    /**
     * 查找所有借阅记录
     * @return 借阅记录列表
     */
    List<BorrowRecord> findAll();
}
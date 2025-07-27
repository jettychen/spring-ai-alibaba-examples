package com.alibaba.cloud.ai.ddd.book.infrastructure.repository;

import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecord;
import com.alibaba.cloud.ai.ddd.book.domain.model.BorrowRecordId;
import com.alibaba.cloud.ai.ddd.book.domain.model.BookId;
import com.alibaba.cloud.ai.ddd.book.domain.repository.BorrowRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 借阅记录仓储实现
 */
@Repository
public class BorrowRecordRepositoryImpl implements BorrowRecordRepository {
    
    // 模拟数据库存储
    private final Map<BorrowRecordId, BorrowRecord> borrowRecords = new ConcurrentHashMap<>();
    
    @Override
    public void save(BorrowRecord borrowRecord) {
        borrowRecords.put(borrowRecord.getId(), borrowRecord);
    }
    
    @Override
    public Optional<BorrowRecord> findById(BorrowRecordId borrowRecordId) {
        return Optional.ofNullable(borrowRecords.get(borrowRecordId));
    }
    
    @Override
    public Optional<BorrowRecord> findByBookIdAndStudentIdAndNotReturned(BookId bookId, String studentId) {
        return borrowRecords.values().stream()
                .filter(record -> record.getBookId().equals(bookId) &&
                                record.getStudentId().equals(studentId) &&
                                !record.isReturned())
                .findFirst();
    }
    
    @Override
    public List<BorrowRecord> findByStudentId(String studentId) {
        return borrowRecords.values().stream()
                .filter(record -> record.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BorrowRecord> findAll() {
        return new ArrayList<>(borrowRecords.values());
    }
}
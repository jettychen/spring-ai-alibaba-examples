package com.alibaba.cloud.ai.ddd.book.domain.model;

import com.alibaba.cloud.ai.ddd.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class BorrowRecordId extends ValueObject {
    
    private final String id;
    
    private BorrowRecordId(String id) {
        this.id = id;
    }
    
    public static BorrowRecordId of(String id) {
        return new BorrowRecordId(id);
    }
    
    public static BorrowRecordId generate() {
        return new BorrowRecordId(UUID.randomUUID().toString());
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowRecordId that = (BorrowRecordId) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BorrowRecordId{" +
                "id='" + id + '\'' +
                '}';
    }
}
package com.alibaba.cloud.ai.ddd.book.domain.model;

import com.alibaba.cloud.ai.ddd.shared.domain.ValueObject;

import java.util.Objects;

public class BookId extends ValueObject {
    
    private final String id;
    
    private BookId(String id) {
        this.id = id;
    }
    
    public static BookId of(String id) {
        return new BookId(id);
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookId bookId = (BookId) o;
        return Objects.equals(id, bookId.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BookId{" +
                "id='" + id + '\'' +
                '}';
    }
}
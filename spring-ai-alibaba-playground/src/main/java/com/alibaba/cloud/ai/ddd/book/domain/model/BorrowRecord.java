package com.alibaba.cloud.ai.ddd.book.domain.model;

import com.alibaba.cloud.ai.ddd.shared.domain.Entity;

import java.time.LocalDateTime;

public class BorrowRecord extends Entity<BorrowRecordId> {
    
    private BorrowRecordId id;
    private BookId bookId;
    private String studentId;
    private String studentName;
    private LocalDateTime borrowTime;
    private LocalDateTime returnTime;
    private boolean returned;
    
    // Constructors
    public BorrowRecord() {}
    
    public BorrowRecord(BorrowRecordId id, BookId bookId, String studentId, String studentName) {
        this.id = id;
        this.bookId = bookId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.borrowTime = LocalDateTime.now();
        this.returned = false;
    }
    
    @Override
    public BorrowRecordId getId() {
        return id;
    }
    
    // Getters and Setters
    public BookId getBookId() {
        return bookId;
    }
    
    public void setBookId(BookId bookId) {
        this.bookId = bookId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public LocalDateTime getBorrowTime() {
        return borrowTime;
    }
    
    public void setBorrowTime(LocalDateTime borrowTime) {
        this.borrowTime = borrowTime;
    }
    
    public LocalDateTime getReturnTime() {
        return returnTime;
    }
    
    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }
    
    public boolean isReturned() {
        return returned;
    }
    
    public void setReturned(boolean returned) {
        this.returned = returned;
    }
    
    /**
     * 归还图书
     */
    public void returnBook() {
        this.returned = true;
        this.returnTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "BorrowRecord{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", borrowTime=" + borrowTime +
                ", returnTime=" + returnTime +
                ", returned=" + returned +
                '}';
    }
}
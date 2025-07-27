package com.alibaba.cloud.ai.ddd.book.domain.model;

import com.alibaba.cloud.ai.ddd.shared.domain.Entity;

import java.util.List;

public class Book extends Entity<BookId> {
    
    private BookId id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String category;
    private List<String> tags;
    private String coverImageUrl;
    private boolean available;
    
    // Constructors
    public Book() {}
    
    public Book(BookId id, String title, String author, String isbn, String description, 
                String category, List<String> tags, String coverImageUrl, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.category = category;
        this.tags = tags;
        this.coverImageUrl = coverImageUrl;
        this.available = available;
    }
    
    @Override
    public BookId getId() {
        return id;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getCoverImageUrl() {
        return coverImageUrl;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", tags=" + tags +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", available=" + available +
                '}';
    }
}
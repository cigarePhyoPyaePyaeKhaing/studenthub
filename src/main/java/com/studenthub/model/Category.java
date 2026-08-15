package com.studenthub.model;

public record Category(long categoryId, String categoryName) {
    public long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
}

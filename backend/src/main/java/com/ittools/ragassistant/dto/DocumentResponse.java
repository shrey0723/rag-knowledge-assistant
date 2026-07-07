package com.ittools.ragassistant.dto;

import java.time.LocalDateTime;

public class DocumentResponse {
    private Long id;
    private String title;
    private String sourceType;
    private LocalDateTime uploadedAt;
    private int chunkCount;

    public DocumentResponse() {}

    public DocumentResponse(Long id, String title, String sourceType, LocalDateTime uploadedAt, int chunkCount) {
        this.id = id;
        this.title = title;
        this.sourceType = sourceType;
        this.uploadedAt = uploadedAt;
        this.chunkCount = chunkCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }
}

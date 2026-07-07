package com.ittools.ragassistant.dto;

public class ChunkDto {
    private Long id;
    private int chunkIndex;
    private String content;

    public ChunkDto() {}

    public ChunkDto(Long id, int chunkIndex, String content) {
        this.id = id;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

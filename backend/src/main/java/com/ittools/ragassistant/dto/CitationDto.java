package com.ittools.ragassistant.dto;

public class CitationDto {
    private Long documentId;
    private String documentTitle;
    private int chunkIndex;
    private String snippet;
    private double score;

    public CitationDto() {}

    public CitationDto(Long documentId, String documentTitle, int chunkIndex, String snippet, double score) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.chunkIndex = chunkIndex;
        this.snippet = snippet;
        this.score = score;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}

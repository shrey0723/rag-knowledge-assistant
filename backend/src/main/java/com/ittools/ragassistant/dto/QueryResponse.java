package com.ittools.ragassistant.dto;

import java.util.List;

public class QueryResponse {
    private Long queryLogId;
    private String question;
    private String answer;
    private List<CitationDto> citations;

    public QueryResponse() {}

    public QueryResponse(Long queryLogId, String question, String answer, List<CitationDto> citations) {
        this.queryLogId = queryLogId;
        this.question = question;
        this.answer = answer;
        this.citations = citations;
    }

    public Long getQueryLogId() { return queryLogId; }
    public void setQueryLogId(Long queryLogId) { this.queryLogId = queryLogId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<CitationDto> getCitations() { return citations; }
    public void setCitations(List<CitationDto> citations) { this.citations = citations; }
}

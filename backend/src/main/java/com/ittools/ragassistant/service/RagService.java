package com.ittools.ragassistant.service;

import com.ittools.ragassistant.dto.*;
import com.ittools.ragassistant.model.Chunk;
import com.ittools.ragassistant.model.Document;
import com.ittools.ragassistant.model.QueryLog;
import com.ittools.ragassistant.repository.ChunkRepository;
import com.ittools.ragassistant.repository.DocumentRepository;
import com.ittools.ragassistant.repository.QueryLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final int TOP_K = 4;

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final QueryLogRepository queryLogRepository;
    private final GeminiClient geminiClient;
    private final ChunkingService chunkingService;

    public RagService(DocumentRepository documentRepository,
                       ChunkRepository chunkRepository,
                       QueryLogRepository queryLogRepository,
                       GeminiClient geminiClient,
                       ChunkingService chunkingService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.queryLogRepository = queryLogRepository;
        this.geminiClient = geminiClient;
        this.chunkingService = chunkingService;
    }

    /** Ingests a document: chunk -> embed each chunk -> store. */
    public DocumentResponse ingestDocument(DocumentRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setSourceType(request.getSourceType() == null || request.getSourceType().isBlank()
                ? "manual" : request.getSourceType());
        documentRepository.save(document);

        List<String> pieces = chunkingService.chunk(request.getContent());
        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            String piece = pieces.get(i);
            double[] embedding = geminiClient.embed(piece);

            Chunk chunk = new Chunk();
            chunk.setDocument(document);
            chunk.setChunkIndex(i);
            chunk.setContent(piece);
            chunk.setEmbedding(serializeEmbedding(embedding));
            chunk.setTokenCount(Math.max(1, piece.length() / 4));
            chunks.add(chunk);
        }
        chunkRepository.saveAll(chunks);
        document.setChunks(chunks);

        return toDocumentResponse(document, chunks.size());
    }

    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .map(d -> toDocumentResponse(d, chunkRepository.findByDocumentId(d.getId()).size()))
                .collect(Collectors.toList());
    }

    public List<ChunkDto> listChunks(Long documentId) {
        return chunkRepository.findByDocumentId(documentId).stream()
                .map(c -> new ChunkDto(c.getId(), c.getChunkIndex(), c.getContent()))
                .collect(Collectors.toList());
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new NoSuchElementException("Document not found: " + id);
        }
        documentRepository.deleteById(id);
    }

    /** Embeds the question, retrieves the top-K most similar chunks, and generates a grounded answer. */
    public QueryResponse query(QueryRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("question is required");
        }

        List<Chunk> allChunks = chunkRepository.findAll();
        if (allChunks.isEmpty()) {
            return new QueryResponse(null, request.getQuestion(),
                    "There are no documents ingested yet. Upload some documents first so I have something to answer from.",
                    List.of());
        }

        double[] questionEmbedding = geminiClient.embed(request.getQuestion());

        List<ScoredChunk> scored = new ArrayList<>();
        for (Chunk chunk : allChunks) {
            double[] chunkEmbedding = deserializeEmbedding(chunk.getEmbedding());
            double score = cosineSimilarity(questionEmbedding, chunkEmbedding);
            scored.add(new ScoredChunk(chunk, score));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<ScoredChunk> topChunks = scored.subList(0, Math.min(TOP_K, scored.size()));

        StringBuilder context = new StringBuilder();
        List<CitationDto> citations = new ArrayList<>();
        for (ScoredChunk sc : topChunks) {
            Chunk c = sc.chunk;
            String title = c.getDocument().getTitle();
            context.append("[Source: ").append(title).append(", excerpt #").append(c.getChunkIndex()).append("]\n");
            context.append(c.getContent()).append("\n\n");
            citations.add(new CitationDto(c.getDocument().getId(), title, c.getChunkIndex(),
                    snippet(c.getContent()), Math.round(sc.score * 1000.0) / 1000.0));
        }

        String prompt = "You are an internal IT/HR knowledge assistant. Answer the question using ONLY the " +
                "context excerpts below. If the answer isn't contained in the context, say clearly that you " +
                "don't have that information rather than guessing.\n\n" +
                "Context:\n" + context +
                "\nQuestion: " + request.getQuestion() + "\nAnswer:";

        String answer = geminiClient.generate(prompt);

        QueryLog log = new QueryLog();
        log.setQuestion(request.getQuestion());
        log.setAnswer(answer);
        log.setRetrievedChunkIds(topChunks.stream()
                .map(sc -> String.valueOf(sc.chunk.getId()))
                .collect(Collectors.joining(",")));
        queryLogRepository.save(log);

        return new QueryResponse(log.getId(), request.getQuestion(), answer, citations);
    }

    public List<QueryLogResponse> recentQueries(int limit) {
        return queryLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(q -> new QueryLogResponse(q.getId(), q.getQuestion(), q.getAnswer(), q.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private String snippet(String content) {
        return content.length() > 220 ? content.substring(0, 220) + "..." : content;
    }

    private DocumentResponse toDocumentResponse(Document d, int chunkCount) {
        return new DocumentResponse(d.getId(), d.getTitle(), d.getSourceType(), d.getUploadedAt(), chunkCount);
    }

    private String serializeEmbedding(double[] embedding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        return sb.toString();
    }

    private double[] deserializeEmbedding(String s) {
        String[] parts = s.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) result[i] = Double.parseDouble(parts[i]);
        return result;
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static class ScoredChunk {
        final Chunk chunk;
        final double score;
        ScoredChunk(Chunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}

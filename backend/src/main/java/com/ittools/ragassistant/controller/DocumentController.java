package com.ittools.ragassistant.controller;

import com.ittools.ragassistant.dto.ChunkDto;
import com.ittools.ragassistant.dto.DocumentRequest;
import com.ittools.ragassistant.dto.DocumentResponse;
import com.ittools.ragassistant.service.RagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final RagService ragService;

    public DocumentController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping
    public List<DocumentResponse> list() {
        return ragService.listDocuments();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DocumentRequest request) {
        try {
            DocumentResponse response = ragService.ingestDocument(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/chunks")
    public List<ChunkDto> chunks(@PathVariable Long id) {
        return ragService.listChunks(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            ragService.deleteDocument(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

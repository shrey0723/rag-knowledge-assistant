package com.ittools.ragassistant.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits raw document text into fixed-size, overlapping chunks.
 * Overlap avoids losing meaning at chunk boundaries. A real production
 * pipeline might chunk on semantic boundaries (headings, paragraphs) instead
 * of a fixed character count - noted here as a natural extension point.
 */
@Service
public class ChunkingService {

    private static final int MAX_CHARS = 800;
    private static final int OVERLAP = 120;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        String normalized = text.trim();
        int length = normalized.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + MAX_CHARS, length);
            String piece = normalized.substring(start, end).trim();
            if (!piece.isEmpty()) chunks.add(piece);
            if (end == length) break;
            start = Math.max(0, end - OVERLAP);
        }
        return chunks;
    }
}

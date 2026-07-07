package com.ittools.ragassistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Google's Gemini API (generativelanguage.googleapis.com).
 * Two endpoints are used:
 *  - embedContent: turns text into a vector for similarity search
 *  - generateContent: generates a grounded answer from a prompt
 *
 * NOTE: Google periodically renames/deprecates model ids. If you get a 404
 * "model not found" error, check https://ai.google.dev for the current
 * model names and update application.properties (gemini.embedding.model /
 * gemini.generation.model) accordingly.
 */
@Service
public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/";

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.embedding.model:models/text-embedding-004}")
    private String embeddingModel;

    @Value("${gemini.generation.model:models/gemini-2.5-flash}")
    private String generationModel;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public double[] embed(String text) {
        checkApiKey();
        String url = BASE_URL + embeddingModel + ":embedContent?key=" + apiKey;

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", text)));

        Map<String, Object> body = new HashMap<>();
        body.put("model", embeddingModel);
        body.put("content", content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Gemini embedding request failed: " + e.getMessage(), e);
        }

        if (response == null || !response.containsKey("embedding")) {
            throw new RuntimeException("Gemini embedding API returned no result. Check your API key and model name.");
        }
        Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
        List<Double> values = (List<Double>) embedding.get("values");
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) result[i] = values.get(i);
        return result;
    }

    @SuppressWarnings("unchecked")
    public String generate(String prompt) {
        checkApiKey();
        String url = BASE_URL + generationModel + ":generateContent?key=" + apiKey;

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> contentEntry = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(contentEntry));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            return "I couldn't reach the Gemini API (" + e.getMessage() + "). Check your API key and network connection.";
        }

        if (response == null || !response.containsKey("candidates")) {
            return "I couldn't generate an answer right now (no candidates returned - this can happen if the " +
                    "response was blocked by safety filters, or the model name is out of date).";
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates.isEmpty()) {
            return "I couldn't generate an answer right now (empty candidates list).";
        }
        Map<String, Object> contentObj = (Map<String, Object>) candidates.get(0).get("content");
        if (contentObj == null) return "I couldn't generate an answer right now (no content in candidate).";
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentObj.get("parts");
        if (parts == null || parts.isEmpty()) return "I couldn't generate an answer right now (no parts in content).";
        Object text = parts.get(0).get("text");
        return text != null ? text.toString() : "I couldn't extract an answer from the model response.";
    }

    private void checkApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set. Set it as an environment variable before starting the backend.");
        }
    }
}

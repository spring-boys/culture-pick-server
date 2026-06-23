package com.ssafy.culturepick.review.client;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class GmsClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GmsClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Value("${gms.api.url}")
    private String apiUrl;

    @Value("${gms.api.key}")
    private String apiKey;

    public String generateText(String systemPrompt, String userContent) {
        GmsResponse response = restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(GmsRequest.of(systemPrompt, userContent))
                .retrieve()
                .body(GmsResponse.class);

        if (response == null) return "";
        return response.extractText();
    }

    public void streamGenerateText(String systemPrompt, String userContent, SseEmitter emitter) {
        try {
            String body = objectMapper.writeValueAsString(GmsRequest.ofStream(systemPrompt, userContent));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) break;
                    try {
                        GmsStreamChunk chunk = objectMapper.readValue(data, GmsStreamChunk.class);
                        String delta = chunk.extractDelta();
                        if (!delta.isEmpty()) {
                            emitter.send(delta);
                        }
                    } catch (Exception ignored) {}
                }
            }
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}

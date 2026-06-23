package com.ssafy.culturepick.review.client;

import lombok.Getter;

import java.util.List;

@Getter
public class GmsRequest {

    private final String model = "gpt-4o-mini";
    private final List<Message> messages;
    private final boolean stream;

    private GmsRequest(List<Message> messages, boolean stream) {
        this.messages = messages;
        this.stream = stream;
    }

    public static GmsRequest of(String systemPrompt, String userContent) {
        return new GmsRequest(List.of(
                new Message("developer", systemPrompt),
                new Message("user", userContent)
        ), false);
    }

    public static GmsRequest ofStream(String systemPrompt, String userContent) {
        return new GmsRequest(List.of(
                new Message("developer", systemPrompt),
                new Message("user", userContent)
        ), true);
    }

    @Getter
    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

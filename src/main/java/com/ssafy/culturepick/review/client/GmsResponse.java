package com.ssafy.culturepick.review.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GmsResponse {

    private List<Choice> choices;

    public String extractText() {
        if (choices == null || choices.isEmpty()) return "";
        Message message = choices.get(0).getMessage();
        if (message == null) return "";
        return message.getContent();
    }

    @Getter
    @Setter
    public static class Choice {
        private Message message;
    }

    @Getter
    @Setter
    public static class Message {
        private String content;
    }
}

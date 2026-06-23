package com.ssafy.culturepick.review.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GmsStreamChunk {

    private List<Choice> choices;

    public String extractDelta() {
        if (choices == null || choices.isEmpty()) return "";
        Delta delta = choices.get(0).getDelta();
        if (delta == null || delta.getContent() == null) return "";
        return delta.getContent();
    }

    @Getter
    @Setter
    public static class Choice {
        private Delta delta;
    }

    @Getter
    @Setter
    public static class Delta {
        private String content;
    }
}

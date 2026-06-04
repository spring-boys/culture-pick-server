package com.ssafy.culturepick.chat.dto.response;

import lombok.Getter;

@Getter
public class ChatMessageEventResponse {

    private String type;
    private ChatMessageResponse message;

    private ChatMessageEventResponse(String type, ChatMessageResponse message) {
        this.type = type;
        this.message = message;
    }

    public static ChatMessageEventResponse created(ChatMessageResponse message) {
        return new ChatMessageEventResponse("MESSAGE_CREATED", message);
    }

    public static ChatMessageEventResponse deleted(ChatMessageResponse message) {
        return new ChatMessageEventResponse("MESSAGE_DELETED", message);
    }
}

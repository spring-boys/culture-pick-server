package com.ssafy.culturepick.chat.dto.response;

import com.ssafy.culturepick.chat.domain.ChatMessage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {

    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private ChatMessageResponse(Long id, Long chatRoomId, Long senderId, String senderNickname,
                                String content, boolean deleted, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getMember().getId(),
                message.getMember().getNickname(),
                message.getDisplayContent(),
                message.isDeleted(),
                message.getCreatedAt(),
                message.getDeletedAt()
        );
    }
}

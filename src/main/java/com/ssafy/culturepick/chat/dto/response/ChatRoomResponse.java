package com.ssafy.culturepick.chat.dto.response;

import com.ssafy.culturepick.chat.domain.ChatRoomMember;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatRoomResponse {

    private Long id;
    private Long cultureId;
    private String cultureTitle;
    private String name;
    private LocalDateTime lastMessageAt;
    private LocalDateTime joinedAt;

    private ChatRoomResponse(Long id, Long cultureId, String cultureTitle, String name,
                             LocalDateTime lastMessageAt, LocalDateTime joinedAt) {
        this.id = id;
        this.cultureId = cultureId;
        this.cultureTitle = cultureTitle;
        this.name = name;
        this.lastMessageAt = lastMessageAt;
        this.joinedAt = joinedAt;
    }

    public static ChatRoomResponse from(ChatRoomMember chatRoomMember) {
        return new ChatRoomResponse(
                chatRoomMember.getChatRoom().getId(),
                chatRoomMember.getChatRoom().getCulture().getId(),
                chatRoomMember.getChatRoom().getCulture().getTitle(),
                chatRoomMember.getChatRoom().getName(),
                chatRoomMember.getChatRoom().getLastMessageAt(),
                chatRoomMember.getJoinedAt()
        );
    }
}

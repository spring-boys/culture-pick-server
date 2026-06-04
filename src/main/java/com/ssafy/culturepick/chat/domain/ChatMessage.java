package com.ssafy.culturepick.chat.domain;

import com.ssafy.culturepick.global.domain.BaseEntity;
import com.ssafy.culturepick.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    public static final String DELETED_MESSAGE_CONTENT = "삭제된 메시지입니다.";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "chat_room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_message_chat_room")
    )
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_message_member")
    )
    private Member member;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ChatMessage(ChatRoom chatRoom, Member member, String content) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.content = content;
        this.deleted = false;
    }

    public static ChatMessage create(ChatRoom chatRoom, Member member, String content) {
        return new ChatMessage(chatRoom, member, content);
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public String getDisplayContent() {
        if (deleted) {
            return DELETED_MESSAGE_CONTENT;
        }
        return content;
    }
}

package com.ssafy.culturepick.chat.domain;

import com.ssafy.culturepick.culture.domain.CultureTemp;
import com.ssafy.culturepick.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room_culture", columnNames = "culture_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "culture_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_culture_temp")
    )
    private CultureTemp culture;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    private ChatRoom(CultureTemp culture, String name) {
        this.culture = culture;
        this.name = name;
    }

    public static ChatRoom create(CultureTemp culture, String name) {
        return new ChatRoom(culture, name);
    }

    public void updateLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}

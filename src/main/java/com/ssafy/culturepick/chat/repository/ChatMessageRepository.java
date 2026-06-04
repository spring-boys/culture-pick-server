package com.ssafy.culturepick.chat.repository;

import com.ssafy.culturepick.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoom_IdOrderByCreatedAtAsc(Long chatRoomId);

    Optional<ChatMessage> findByIdAndChatRoom_Id(Long id, Long chatRoomId);
}

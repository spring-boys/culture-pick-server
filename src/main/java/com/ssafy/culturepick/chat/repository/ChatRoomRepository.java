package com.ssafy.culturepick.chat.repository;

import com.ssafy.culturepick.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByCulture_Id(Long cultureId);

    boolean existsByCulture_Id(Long cultureId);
}

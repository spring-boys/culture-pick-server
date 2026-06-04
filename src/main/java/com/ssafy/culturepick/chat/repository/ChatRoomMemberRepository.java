package com.ssafy.culturepick.chat.repository;

import com.ssafy.culturepick.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    Optional<ChatRoomMember> findByChatRoom_IdAndMember_Id(Long chatRoomId, Long memberId);

    boolean existsByChatRoom_IdAndMember_IdAndActiveTrue(Long chatRoomId, Long memberId);

    List<ChatRoomMember> findByMember_IdAndActiveTrueOrderByJoinedAtDesc(Long memberId);
}

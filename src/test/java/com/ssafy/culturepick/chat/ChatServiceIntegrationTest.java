package com.ssafy.culturepick.chat;

import com.ssafy.culturepick.chat.domain.ChatMessage;
import com.ssafy.culturepick.chat.domain.ChatRoom;
import com.ssafy.culturepick.chat.domain.ChatRoomMember;
import com.ssafy.culturepick.chat.dto.request.ChatMessageSendRequest;
import com.ssafy.culturepick.chat.dto.response.ChatMessageResponse;
import com.ssafy.culturepick.chat.dto.response.ChatRoomResponse;
import com.ssafy.culturepick.chat.repository.ChatMessageRepository;
import com.ssafy.culturepick.chat.repository.ChatRoomMemberRepository;
import com.ssafy.culturepick.chat.repository.ChatRoomRepository;
import com.ssafy.culturepick.chat.service.ChatService;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.ChatErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ChatServiceIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CultureRepository cultureRepository;

    private Member member1;
    private Member member2;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(Member.createLocalMember("member1@test.com", "password", "회원1"));
        member2 = memberRepository.save(Member.createLocalMember("member2@test.com", "password", "회원2"));

        Culture culture = cultureRepository.save(Culture.builder()
                .seq(9999L)
                .title("테스트 전시회")
                .build());

        chatRoom = chatRoomRepository.save(ChatRoom.create(culture, "테스트 전시회 채팅방"));
    }

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        cultureRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("처음 입장하면 active=true 인 ChatRoomMember가 저장된다")
    void joinChatRoom_success_firstJoin() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());

        ChatRoomMember saved = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(chatRoom.getId(), member1.getId())
                .orElseThrow();

        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("퇴장 후 재입장하면 새 레코드 없이 기존 레코드가 active=true 로 복원된다")
    void joinChatRoom_success_rejoin() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());

        ChatRoomMember original = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(chatRoom.getId(), member1.getId())
                .orElseThrow();
        original.leave();
        chatRoomMemberRepository.save(original);

        chatService.joinChatRoom(chatRoom.getId(), member1.getId());

        List<ChatRoomMember> all = chatRoomMemberRepository.findAll().stream()
                .filter(m -> m.getChatRoom().getId().equals(chatRoom.getId())
                        && m.getMember().getId().equals(member1.getId()))
                .toList();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("이미 활성 상태로 입장 중인 경우 재호출해도 에러 없이 active=true 유지된다")
    void joinChatRoom_success_alreadyActive() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());

        boolean active = chatRoomMemberRepository
                .existsByChatRoom_IdAndMember_IdAndActiveTrue(chatRoom.getId(), member1.getId());

        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 채팅방에 입장하면 CHAT_ROOM_NOT_FOUND 예외가 발생한다")
    void joinChatRoom_fail_chatRoomNotFound() {
        Long nonExistentChatRoomId = -1L;

        assertThatThrownBy(() -> chatService.joinChatRoom(nonExistentChatRoomId, member1.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    // ===================== getMessages =====================

    @Test
    @DisplayName("채팅방 멤버가 메시지 목록을 조회하면 createdAt 오름차순으로 반환된다")
    void getMessages_success_orderedByCreatedAt() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("첫 번째 메시지").build());
        chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("두 번째 메시지").build());

        List<ChatMessageResponse> messages = chatService.getMessages(chatRoom.getId(), member1.getId());

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getContent()).isEqualTo("첫 번째 메시지");
        assertThat(messages.get(1).getContent()).isEqualTo("두 번째 메시지");
    }

    @Test
    @DisplayName("삭제된 메시지는 '삭제된 메시지입니다.'로 반환된다")
    void getMessages_success_deletedMessageContent() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        ChatMessageResponse sent = chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("삭제될 메시지").build());
        chatService.deleteMessage(chatRoom.getId(), sent.getId(), member1.getId());

        List<ChatMessageResponse> messages = chatService.getMessages(chatRoom.getId(), member1.getId());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo(ChatMessage.DELETED_MESSAGE_CONTENT);
        assertThat(messages.get(0).isDeleted()).isTrue();
    }

    @Test
    @DisplayName("채팅방 비회원이 메시지 목록을 조회하면 NOT_CHAT_ROOM_MEMBER 예외가 발생한다")
    void getMessages_fail_notMember() {
        assertThatThrownBy(() -> chatService.getMessages(chatRoom.getId(), member1.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ChatErrorCode.NOT_CHAT_ROOM_MEMBER);
    }

    // ===================== sendMessage =====================

    @Test
    @DisplayName("채팅방 멤버가 메시지를 전송하면 저장되고 ChatRoom의 lastMessageAt이 갱신된다")
    void sendMessage_success() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        ChatMessageResponse response = chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("안녕하세요").build());

        ChatMessage saved = chatMessageRepository.findById(response.getId()).orElseThrow();
        ChatRoom updatedRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();

        assertThat(saved.getContent()).isEqualTo("안녕하세요");
        assertThat(saved.isDeleted()).isFalse();
        assertThat(updatedRoom.getLastMessageAt()).isNotNull();
        assertThat(updatedRoom.getLastMessageAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    @DisplayName("채팅방 비회원이 메시지를 전송하면 NOT_CHAT_ROOM_MEMBER 예외가 발생한다")
    void sendMessage_fail_notMember() {
        assertThatThrownBy(() -> chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("비회원 메시지").build()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ChatErrorCode.NOT_CHAT_ROOM_MEMBER);
    }

    @Test
    @DisplayName("본인 메시지를 삭제하면 deleted=true 가 되고 deletedAt이 세팅된다")
    void deleteMessage_success() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        ChatMessageResponse sent = chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("삭제할 메시지").build());

        chatService.deleteMessage(chatRoom.getId(), sent.getId(), member1.getId());

        ChatMessage deleted = chatMessageRepository.findById(sent.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getDisplayContent()).isEqualTo(ChatMessage.DELETED_MESSAGE_CONTENT);
    }

    @Test
    @DisplayName("다른 회원의 메시지를 삭제하면 NOT_MESSAGE_OWNER 예외가 발생한다")
    void deleteMessage_fail_notOwner() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        chatService.joinChatRoom(chatRoom.getId(), member2.getId());
        ChatMessageResponse sent = chatService.sendMessage(chatRoom.getId(), member1.getId(), ChatMessageSendRequest.builder().content("회원1 메시지").build());

        assertThatThrownBy(() -> chatService.deleteMessage(chatRoom.getId(), sent.getId(), member2.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ChatErrorCode.NOT_MESSAGE_OWNER);
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 삭제하면 CHAT_MESSAGE_NOT_FOUND 예외가 발생한다")
    void deleteMessage_fail_messageNotFound() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());
        Long nonExistentMessageId = -1L;

        assertThatThrownBy(() -> chatService.deleteMessage(chatRoom.getId(), nonExistentMessageId, member1.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ChatErrorCode.CHAT_MESSAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("내가 활성 입장한 채팅방 목록이 joinedAt 내림차순으로 반환된다")
    void getMyChatRooms_success() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());

        List<ChatRoomResponse> rooms = chatService.getMyChatRooms(member1.getId());

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getId()).isEqualTo(chatRoom.getId());
        assertThat(rooms.get(0).getCultureTitle()).isEqualTo("테스트 전시회");
    }

    @Test
    @DisplayName("퇴장한 채팅방은 내 채팅방 목록에 포함되지 않는다")
    void getMyChatRooms_excludesInactiveRooms() {
        chatService.joinChatRoom(chatRoom.getId(), member1.getId());

        ChatRoomMember membership = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(chatRoom.getId(), member1.getId())
                .orElseThrow();
        membership.leave();
        chatRoomMemberRepository.save(membership);

        List<ChatRoomResponse> rooms = chatService.getMyChatRooms(member1.getId());

        assertThat(rooms).isEmpty();
    }
}

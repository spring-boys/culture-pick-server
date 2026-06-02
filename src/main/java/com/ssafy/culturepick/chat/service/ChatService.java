package com.ssafy.culturepick.chat.service;

import com.ssafy.culturepick.chat.domain.ChatMessage;
import com.ssafy.culturepick.chat.domain.ChatRoom;
import com.ssafy.culturepick.chat.domain.ChatRoomMember;
import com.ssafy.culturepick.chat.dto.request.ChatMessageSendRequest;
import com.ssafy.culturepick.chat.dto.response.ChatMessageResponse;
import com.ssafy.culturepick.chat.dto.response.ChatRoomResponse;
import com.ssafy.culturepick.chat.repository.ChatMessageRepository;
import com.ssafy.culturepick.chat.repository.ChatRoomMemberRepository;
import com.ssafy.culturepick.chat.repository.ChatRoomRepository;
import com.ssafy.culturepick.global.exception.code.ChatErrorCode;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        Member member = getMember(memberId);

        chatRoomMemberRepository.findByChatRoom_IdAndMember_Id(chatRoomId, memberId)
                .ifPresentOrElse(
                        chatRoomMember -> {
                            if (!chatRoomMember.isActive()) {
                                chatRoomMember.rejoin();
                            }
                        },
                        () -> chatRoomMemberRepository.save(ChatRoomMember.join(chatRoom, member))
                );
    }

    public List<ChatMessageResponse> getMessages(Long chatRoomId, Long memberId) {
        validateActiveChatRoomMember(chatRoomId, memberId);

        return chatMessageRepository.findByChatRoom_IdOrderByCreatedAtAsc(chatRoomId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, Long memberId, ChatMessageSendRequest request) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        Member member = getMember(memberId);
        validateActiveChatRoomMember(chatRoomId, memberId);

        ChatMessage message = chatMessageRepository.saveAndFlush(ChatMessage.create(chatRoom, member, request.getContent()));
        chatRoom.updateLastMessageAt(message.getCreatedAt());

        return ChatMessageResponse.from(message);
    }

    @Transactional
    public ChatMessageResponse deleteMessage(Long chatRoomId, Long messageId, Long memberId) {
        validateActiveChatRoomMember(chatRoomId, memberId);

        ChatMessage message = chatMessageRepository.findByIdAndChatRoom_Id(messageId, chatRoomId)
                .orElseThrow(() -> new BusinessException(ChatErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (!message.getMember().getId().equals(memberId)) {
            throw new BusinessException(ChatErrorCode.NOT_MESSAGE_OWNER);
        }

        message.delete();
        return ChatMessageResponse.from(message);
    }

    public List<ChatRoomResponse> getMyChatRooms(Long memberId) {
        getMember(memberId);

        return chatRoomMemberRepository.findByMember_IdAndActiveTrueOrderByJoinedAtDesc(memberId).stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    private void validateActiveChatRoomMember(Long chatRoomId, Long memberId) {
        if (!chatRoomMemberRepository.existsByChatRoom_IdAndMember_IdAndActiveTrue(chatRoomId, memberId)) {
            throw new BusinessException(ChatErrorCode.NOT_CHAT_ROOM_MEMBER);
        }
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}

package com.ssafy.culturepick.global.config;

import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.chat.repository.ChatRoomMemberRepository;
import com.ssafy.culturepick.global.exception.code.AuthErrorCode;
import com.ssafy.culturepick.global.exception.code.ChatErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor { //WebSocket 연결 이후 STOMP 메시지에 대한 인증/인가 검사를 직접 수행하기 위한 인터셉터

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String CHAT_ROOM_TOPIC_PREFIX = "/topic/chat-rooms/";

    private final TokenProvider tokenProvider;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) { //클라이언트에서 서버로 들어오는 STOMP 메시지가 처리되기 전에 실행되는 메서드
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) { //JWT 토큰 검증
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) { //해당 채팅방 참여자인지 검증
            validateSubscription(accessor);
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) { // 클라이언트가 /topic/**으로 직접 보내는 잘못된 전송 차단
            validateSendDestination(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));

        if (token == null) {
            throw new MessagingException(AuthErrorCode.UNAUTHORIZED.getMessage());
        }

        try {
            tokenProvider.validateToken(token);
            Authentication authentication = tokenProvider.getAuthentication(token);
            accessor.setUser(authentication);
        } catch (BusinessException e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(CHAT_ROOM_TOPIC_PREFIX)) {
            return;
        }

        Authentication authentication = getAuthentication(accessor);
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long chatRoomId = parseChatRoomId(destination);

        if (!chatRoomMemberRepository.existsByChatRoom_IdAndMember_IdAndActiveTrue(chatRoomId, memberDetails.getId())) {
            throw new MessagingException(ChatErrorCode.NOT_CHAT_ROOM_MEMBER.getMessage());
        }
    }

    private void validateSendDestination(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith(CHAT_ROOM_TOPIC_PREFIX)) {
            throw new MessagingException(AuthErrorCode.FORBIDDEN.getMessage());
        }
    }

    private Authentication getAuthentication(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication)) {
            throw new MessagingException(AuthErrorCode.UNAUTHORIZED.getMessage());
        }
        return authentication;
    }

    private Long parseChatRoomId(String destination) {
        try {
            return Long.valueOf(destination.substring(CHAT_ROOM_TOPIC_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new MessagingException(ChatErrorCode.CHAT_ROOM_NOT_FOUND.getMessage(), e);
        }
    }

    private String resolveToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}

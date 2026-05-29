package com.ssafy.culturepick.chat.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.chat.dto.request.ChatMessageSendRequest;
import com.ssafy.culturepick.chat.dto.response.ChatMessageEventResponse;
import com.ssafy.culturepick.chat.dto.response.ChatMessageResponse;
import com.ssafy.culturepick.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat-rooms/{chatRoomId}/messages")
    public void sendMessage(@DestinationVariable Long chatRoomId,
                            @Valid @Payload ChatMessageSendRequest request,
                            Principal principal) {
        CustomMemberDetails memberDetails = getMemberDetails(principal);
        ChatMessageResponse response = chatService.sendMessage(chatRoomId, memberDetails.getId(), request);
        messagingTemplate.convertAndSend("/topic/chat-rooms/" + chatRoomId, ChatMessageEventResponse.created(response));
    }

    private CustomMemberDetails getMemberDetails(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return (CustomMemberDetails) authentication.getPrincipal();
    }
}

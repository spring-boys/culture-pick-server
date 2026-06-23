package com.ssafy.culturepick.chat.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.chat.dto.request.ChatMessageSendRequest;
import com.ssafy.culturepick.chat.dto.response.ChatMessageEventResponse;
import com.ssafy.culturepick.chat.dto.response.ChatMessageResponse;
import com.ssafy.culturepick.chat.dto.response.ChatRoomResponse;
import com.ssafy.culturepick.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-rooms")
public class ChatRoomController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<ChatRoomResponse> joinChatRoom(@PathVariable Long chatRoomId,
                                                         @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(chatService.joinChatRoom(chatRoomId, memberDetails.getId()));
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long chatRoomId,
                                                                 @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(chatService.getMessages(chatRoomId, memberDetails.getId()));
    }

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessageByHttp(@PathVariable Long chatRoomId,
                                                                 @AuthenticationPrincipal CustomMemberDetails memberDetails,
                                                                 @Valid @RequestBody ChatMessageSendRequest request) {
        ChatMessageResponse response = chatService.sendMessage(chatRoomId, memberDetails.getId(), request);
        messagingTemplate.convertAndSend(getChatRoomTopic(chatRoomId), ChatMessageEventResponse.created(response));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{chatRoomId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long chatRoomId,
                                              @PathVariable Long messageId,
                                              @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        ChatMessageResponse response = chatService.deleteMessage(chatRoomId, messageId, memberDetails.getId());
        messagingTemplate.convertAndSend(getChatRoomTopic(chatRoomId), ChatMessageEventResponse.deleted(response));
        return ResponseEntity.noContent().build();
    }

    private String getChatRoomTopic(Long chatRoomId) {
        return "/topic/chat-rooms/" + chatRoomId;
    }
}

package com.ssafy.culturepick.chat.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.chat.dto.response.ChatRoomResponse;
import com.ssafy.culturepick.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/chat-rooms")
public class MyPageChatRoomController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(@AuthenticationPrincipal CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(chatService.getMyChatRooms(memberDetails.getId()));
    }
}

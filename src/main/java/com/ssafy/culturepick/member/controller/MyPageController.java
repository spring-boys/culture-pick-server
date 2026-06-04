package com.ssafy.culturepick.member.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.member.dto.response.MyPageResponse;
import com.ssafy.culturepick.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(myPageService.getMyPage(memberDetails.getId()));
    }
}

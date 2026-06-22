package com.ssafy.culturepick.member.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.global.common.PageResponse;
import com.ssafy.culturepick.member.dto.request.UpdateMemberInfoRequest;
import com.ssafy.culturepick.member.dto.response.BookmarkedCultureResponse;
import com.ssafy.culturepick.member.dto.response.MyPageResponse;
import com.ssafy.culturepick.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(myPageService.getMyPage(memberDetails.getId()));
    }

    @PutMapping
    public ResponseEntity<Void> updateInfo(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody UpdateMemberInfoRequest request
    ) {
        myPageService.updateInfo(memberDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<PageResponse<BookmarkedCultureResponse>> getBookmarkedCultures(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(myPageService.getBookmarkedCultures(memberDetails.getId(), page));
    }
}

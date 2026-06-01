package com.ssafy.culturepick.bookmark.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cultures/{cultureId}/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<Void> addBookmark(
            @PathVariable Long cultureId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        bookmarkService.addBookmark(memberDetails.getId(), cultureId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @PathVariable Long cultureId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        bookmarkService.removeBookmark(memberDetails.getId(), cultureId);
        return ResponseEntity.ok().build();
    }
}

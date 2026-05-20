package com.ssafy.culturepick.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {

    LOCAL("일반 로그인"),
    GOOGLE("구글 로그인");

    private final String description;
}

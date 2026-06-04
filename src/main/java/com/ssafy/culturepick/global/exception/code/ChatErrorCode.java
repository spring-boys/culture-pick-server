package com.ssafy.culturepick.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    CULTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "문화행사를 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_002", "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_003", "채팅 메시지를 찾을 수 없습니다."),
    ALREADY_JOINED_CHAT_ROOM(HttpStatus.BAD_REQUEST, "CHAT_004", "이미 참여 중인 채팅방입니다."),
    NOT_CHAT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "CHAT_005", "채팅방 참여자만 사용할 수 있습니다."),
    NOT_MESSAGE_OWNER(HttpStatus.FORBIDDEN, "CHAT_006", "본인이 작성한 메시지만 삭제할 수 있습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CHAT_007", "문화행사에 이미 채팅방이 존재합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

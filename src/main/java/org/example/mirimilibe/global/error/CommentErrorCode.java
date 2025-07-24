package org.example.mirimilibe.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT404", "댓글을 찾을 수 없습니다."),
	NO_PERMISSION_TO_COMMENT(HttpStatus.FORBIDDEN, "COMMENT403", "이 게시글에 답변할 권한이 없습니다."),
	ALREADY_REACTED(HttpStatus.BAD_REQUEST, "COMMENT409", "이미 해당 반응을 눌렀습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
package org.example.mirimilibe.global.error;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404", "게시글을 찾을 수 없습니다."),
	NO_PERMISSION_TO_EDIT(HttpStatus.FORBIDDEN, "POST403", "해당 게시글을 수정할 권한이 없습니다."),
	NO_PERMISSION_TO_DELETE(HttpStatus.FORBIDDEN, "POST403", "해당 게시글을 삭제할 권한이 없습니다."),
	INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "POST400", "유효하지 않은 카테고리입니다."),
	ALREADY_REACTED(HttpStatus.BAD_REQUEST, "POST409", "이미 해당 반응을 눌렀습니다."),
	INVALID_SPECIALTY(HttpStatus.BAD_REQUEST, "POST400", "유효하지 않은 특기입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
package org.example.mirimilibe.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MilitaryInfoErrorCode implements ErrorCode{
	MILITARY_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "MILITARYINFO404", "군 정보를 찾을 수 없습니다."),
	MILITARY_INFO_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MILITARYINFO409", "이미 군 정보가 존재합니다."),
	MILITARY_INFO_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "MILITARYINFO400", "군 정보를 수정할 수 없습니다."),
	MILITARY_INFO_CANNOT_ACCESS(HttpStatus.BAD_REQUEST, "MILITARYINFO403", "군 정보에 접근할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

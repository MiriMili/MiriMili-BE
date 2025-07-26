package org.example.mirimilibe.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements ErrorCode {
	SEND_SMS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS500", "SMS 전송에 실패했습니다."),
	INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "SMS400", "유효하지 않은 전화번호입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

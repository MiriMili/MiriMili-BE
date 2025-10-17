package org.example.mirimilibe.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION404", "알림을 찾을 수 없습니다."),
	NO_PERMISSION_TO_READ(HttpStatus.FORBIDDEN, "NOTIFICATION403", "알림을 읽을 권한이 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

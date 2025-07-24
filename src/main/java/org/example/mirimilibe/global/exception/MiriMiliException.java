package org.example.mirimilibe.global.exception;

import org.example.mirimilibe.global.error.ErrorCode;

import ch.qos.logback.core.spi.ErrorCodes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MiriMiliException extends RuntimeException{

	private final ErrorCode errorCode;
}

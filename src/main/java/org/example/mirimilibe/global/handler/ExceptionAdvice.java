package org.example.mirimilibe.global.handler;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.error.CommonErrorCode;
import org.example.mirimilibe.global.error.ErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(MiriMiliException.class)
	public ResponseEntity<Object> handleRestApiException(MiriMiliException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		return handleExceptionInternal(errorCode);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("handleIllegalArgument");
		ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
		return handleExceptionInternal(errorCode, ex.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
		HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.warn("handleHttpRequestMethodNotSupportedException");
		ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
		return handleExceptionInternal(errorCode);
	}

	@Override
	public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
		HttpStatusCode status, WebRequest request) {

		log.warn("MethodArgumentNotValidException ");
		ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
		return handleExceptionInternal(errorCode, getDefaultMessage(e));
	}

	private static String getDefaultMessage(MethodArgumentNotValidException e) {
		StringBuilder message = new StringBuilder();
		for (ObjectError error : e.getBindingResult().getAllErrors()) {
			message.append(error.getDefaultMessage()).append("\u00a0");
		}
		return message.toString();
	}

	private ResponseEntity<Object> handleExceptionInternal(final ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.createFail(errorCode));
	}

	private ResponseEntity<Object> handleExceptionInternal(final ErrorCode errorCode, final String message) {
		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.createFail(errorCode, message));
	}
}
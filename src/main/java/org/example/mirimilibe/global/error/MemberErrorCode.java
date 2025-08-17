package org.example.mirimilibe.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode{
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "멤버를 찾을 수 없습니다."),
	INVALID_MEMBER_PARAMETER(HttpStatus.BAD_REQUEST, "MEMBER400", "유효하지 않는 멤버입니다"),

	REFRESH_EXPIRED(HttpStatus.BAD_REQUEST, "ACCESS401", "리프레시 토큰이 만료되었습니다."),
	ACCESS_TOKEN_NOT_FOUND(HttpStatus.FORBIDDEN,"ACCESS400","액세스 토큰이 없습니다."),
	ACCESS_EXPIRED(HttpStatus.BAD_REQUEST,"ACCESS401","액세스 토큰이 만료되었습니다"),
	DUPLICATE_AUTHORIZE_CODE(HttpStatus.BAD_REQUEST,"ACCESS402","인가 코드 중복 사용"),
	ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN,"ACCESS403","탈퇴한 회원입니다."),
	MILITARY_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404-2", "군 정보를 찾을 수 없습니다."),
	MILITARY_INFO_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER400-1", "이미 군 정보가 존재합니다."),
	UNIT_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404-3", "부대를 찾을 수 없습니다."),
	SPECIALTY_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404-4", "특기를 찾을 수 없습니다."),
	MILITARY_INFO_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "MEMBER400-2", "입력된 정보는 수정할 수 없습니다."),

	MATCH_NOT_FOUND(HttpStatus.NOT_FOUND,"MATCH404","이용내역을 찾을 수 없습니다."),
	DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "MEMBER400-3", "이미 등록된 전화번호입니다."),
	DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER400-4", "이미 등록된 닉네임입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;


}
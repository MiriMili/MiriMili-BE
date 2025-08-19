package org.example.mirimilibe.global.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record SmsReq(
	@Pattern(regexp = "^\\d{11}$", message = "전화번호는 숫자 11자리여야 합니다.")	@NotEmpty(message = "전화번호는 필수입니다.")
	String phoneNumber
) {}

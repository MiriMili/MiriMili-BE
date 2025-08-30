package org.example.mirimilibe.global.auth.dto;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.member.dto.MilitaryInfoReq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpReq(
	@NotNull
	@Pattern(regexp = "^\\d{11}$", message = "전화번호는 숫자 11자리여야 합니다.")
	String phoneNumber,
	@NotBlank
	String password,
	@NotBlank
	String nickname,
	@NotNull
	Boolean serviceAgreed,
	@NotNull
	Boolean privacyPolicyAgreed,
	@NotNull
	Boolean marketingConsentAgreed,
	@NotNull
	MiliStatus miliStatus
) {}

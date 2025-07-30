package org.example.mirimilibe.global.auth.dto;

import org.example.mirimilibe.member.dto.MilitaryInfoReq;

import jakarta.validation.constraints.Pattern;

public record SignUpReq(
	@Pattern(regexp = "^\\d{11}$", message = "전화번호는 숫자 11자리여야 합니다.")
	String phoneNumber,
	String password,
	String nickname,
	Boolean serviceAgreed,
	Boolean privacyPolicyAgreed,
	Boolean marketingConsentAgreed,
	MilitaryInfoReq militaryInfoReq
) {}

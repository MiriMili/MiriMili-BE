package org.example.mirimilibe.global.auth.dto;

import org.example.mirimilibe.member.dto.MilitaryInfoReq;

public record SignUpReq(
	String phoneNumber,
	String password,
	String nickname,
	Boolean serviceAgreed,
	Boolean privacyPolicyAgreed,
	Boolean marketingConsentAgreed,
	MilitaryInfoReq militaryInfoReq
) {}

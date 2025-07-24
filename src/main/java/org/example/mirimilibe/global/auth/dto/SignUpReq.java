package org.example.mirimilibe.global.auth.dto;

public record SignUpReq(
	String phoneNumber,
	String password,
	String nickname
) {}

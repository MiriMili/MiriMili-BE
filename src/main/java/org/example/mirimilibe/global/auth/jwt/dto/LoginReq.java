package org.example.mirimilibe.global.auth.jwt.dto;

public record LoginReq(
	String phoneNumber,
	String password
) {}

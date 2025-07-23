package org.example.mirimilibe.auth.jwt.dto;

public record LoginReq(
	String phoneNumber,
	String password
) {}

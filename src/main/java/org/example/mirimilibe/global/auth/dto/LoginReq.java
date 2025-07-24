package org.example.mirimilibe.global.auth.dto;

public record LoginReq(
	String phoneNumber,
	String password
) {}

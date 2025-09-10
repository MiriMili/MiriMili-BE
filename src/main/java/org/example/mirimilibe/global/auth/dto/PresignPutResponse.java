package org.example.mirimilibe.global.auth.dto;

public record PresignPutResponse(
	String key,
	String uploadUrl
) {}
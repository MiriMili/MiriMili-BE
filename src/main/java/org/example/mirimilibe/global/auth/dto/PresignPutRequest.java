package org.example.mirimilibe.global.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PresignPutRequest(
	@NotBlank String contentType,
	@Positive long contentLength
) {}
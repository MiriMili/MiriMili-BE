package org.example.mirimilibe.global.auth.dto;

import jakarta.validation.constraints.NotEmpty;

public record RefreshDTO() {

	// 요청 DTO
	public record Req(
		@NotEmpty(message = "요청 시 Refresh Token은 필수입니다.")
		String refreshToken
	) {}

	// 응답 DTO
	public record Res(
		String accessToken
	) {
		public static Res of(String accessToken) {
			return new Res(accessToken);
		}
	}
}

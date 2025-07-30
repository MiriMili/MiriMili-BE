package org.example.mirimilibe.global.auth.dto;

public record LoginSuccessRes(
	String accessToken,
	String refreshToken,
	String nickname
) {
	public static LoginSuccessRes of(String accessToken, String refreshToken, String nickname) {
		return new LoginSuccessRes(accessToken, refreshToken, nickname);
	}
}

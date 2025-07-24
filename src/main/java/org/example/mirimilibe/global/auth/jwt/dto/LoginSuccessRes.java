package org.example.mirimilibe.global.auth.jwt.dto;

public record LoginSuccessRes(
	String accessToken,
	String nickname
) {
	public static LoginSuccessRes of(String accessToken, String nickname) {
		return new LoginSuccessRes(accessToken, nickname);
	}
}

package org.example.mirimilibe.global.auth.dto;

public record LoginSuccessRes(
	String accessToken,
	String refreshToken,
	String nickname,
	boolean hasMilitaryInfo
) {
	public static LoginSuccessRes of(String accessToken, String refreshToken, String nickname, boolean hasMilitaryInfo) {
		return new LoginSuccessRes(accessToken, refreshToken, nickname, hasMilitaryInfo);
	}
}

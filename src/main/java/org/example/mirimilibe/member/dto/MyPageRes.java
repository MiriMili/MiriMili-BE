package org.example.mirimilibe.member.dto;

import org.example.mirimilibe.common.Enum.MiliStatus;

public record MyPageRes (
	MiliStatus status,
	String nickname,
	String phoneNumber,
	MilitaryInfoRes militaryInfo
){
	public static MyPageRes of(MiliStatus status, String nickname, String phoneNumber, MilitaryInfoRes militaryInfo) {
		return new MyPageRes(status, nickname, phoneNumber, militaryInfo);
	}
}

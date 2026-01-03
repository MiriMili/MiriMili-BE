package org.example.mirimilibe.post.dto;

import org.example.mirimilibe.common.Enum.MiliRank;
import org.example.mirimilibe.common.Enum.MiliType;

public record PostMyInfoRes (
	String nickName,
	Boolean isAnswerable,
	Long specialtyId,
	MiliType miliType,
	MiliRank miliRank
){ }

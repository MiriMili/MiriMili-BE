package org.example.mirimilibe.post.dto;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;

public record PostMyInfoRes (
	String nickName,
	Boolean isAnswerable,
	String specialty,
	MiliType miliType,
	MiliStatus miliStatus
){ }

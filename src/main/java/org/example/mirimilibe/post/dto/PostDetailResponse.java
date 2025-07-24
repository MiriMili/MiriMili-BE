package org.example.mirimilibe.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;

public record PostDetailResponse(

	Long id,
	String title,
	String body,

	String writerNickname,
	String writerSpecialty,
	MiliStatus writerStatus,

	LocalDateTime createdAt,
	List<String> images,

	List<String> categories,
	MiliType targetMiliType,
	List<String> targetSpecialties,

	Long commentCount,
	Long viewCount

) {}
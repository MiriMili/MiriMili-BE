package org.example.mirimilibe.comment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;

public record CommentSummaryResponse(
	Long id,
	String content,
	List<String> imagesUrl,
	String writerNickname,
	LocalDateTime createdAt,
	long likeCount,
	long dislikeCount,
	boolean matchesTarget, // 게시글 조건과 일치하는 특기 or 군구분을 가진 작성자인가?

	String writerSpecialty,
	MiliType writerMiliType,
	MiliStatus writerStatus
) {}

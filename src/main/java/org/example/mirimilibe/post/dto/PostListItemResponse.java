package org.example.mirimilibe.post.dto;

import java.time.LocalDateTime;

public record PostListItemResponse(
	Long id,
	String title,
	String body,
	Long commentCount,
	Long likeCount,
	Long viewCount,
	LocalDateTime createdAt
) {}
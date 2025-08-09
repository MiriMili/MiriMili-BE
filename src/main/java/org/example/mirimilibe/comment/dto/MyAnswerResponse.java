package org.example.mirimilibe.comment.dto;

public record MyAnswerResponse(
	Long postId,
	String postTitle,
	String myCommentBody
) {}
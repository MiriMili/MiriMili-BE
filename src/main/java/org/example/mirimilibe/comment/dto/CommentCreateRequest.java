package org.example.mirimilibe.comment.dto;

import java.util.List;

public record CommentCreateRequest(
	String body,
	List<String> imagesUrl
) {}
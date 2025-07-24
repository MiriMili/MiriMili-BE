package org.example.mirimilibe.post.dto;

import java.util.List;

import org.example.mirimilibe.common.Enum.MiliType;

public record PostCreateRequest(
	String title,
	String body,
	List<String> imagesUrl,
	MiliType targetMiliType,
	List<Long> categoryIds,
	List<Long> specialtyIds
) {}
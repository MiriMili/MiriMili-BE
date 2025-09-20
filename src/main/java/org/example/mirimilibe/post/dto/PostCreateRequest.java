package org.example.mirimilibe.post.dto;

import java.util.List;

import org.example.mirimilibe.common.Enum.MiliType;

import jakarta.validation.constraints.Size;

public record PostCreateRequest(
	String title,
	String body,
	@Size(max = 5) List<String> imageKeys,
	MiliType targetMiliType,
	List<Long> categoryIds,
	List<Long> specialtyIds
) {}
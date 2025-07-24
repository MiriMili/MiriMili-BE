package org.example.mirimilibe.post.dto;

import org.example.mirimilibe.comment.domain.ReactionType;

public record PostLikeRequest(
	ReactionType type
) {}
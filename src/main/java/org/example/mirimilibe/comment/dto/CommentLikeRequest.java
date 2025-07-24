package org.example.mirimilibe.comment.dto;

import org.example.mirimilibe.comment.domain.ReactionType;

public record CommentLikeRequest(
	ReactionType type

){}

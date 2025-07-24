package org.example.mirimilibe.comment.controller;

import org.example.mirimilibe.comment.dto.CommentLikeRequest;
import org.example.mirimilibe.comment.service.CommentLikeService;
import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.member.domain.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentLikeController {

	private final CommentLikeService commentLikeService;

	@PostMapping("/{commentId}/likes")
	public ResponseEntity<ApiResponse<Void>> likeComment(
		@PathVariable Long commentId,
		@RequestBody CommentLikeRequest req,
		Member user
	) {
		commentLikeService.reactToComment(user.getId(), commentId, req.type());
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
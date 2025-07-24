package org.example.mirimilibe.post.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.dto.PostLikeRequest;
import org.example.mirimilibe.post.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostLikeController {

	private final PostLikeService postLikeService;

	@PostMapping("/{postId}/likes")
	public ResponseEntity<ApiResponse<Void>> likePost(
		@PathVariable Long postId,
		@RequestBody PostLikeRequest req,
		Member member
	) {
		postLikeService.reactToPost(member.getId(), postId, req.type());
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}

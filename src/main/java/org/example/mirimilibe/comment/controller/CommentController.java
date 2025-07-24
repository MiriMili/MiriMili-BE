package org.example.mirimilibe.comment.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.example.mirimilibe.comment.dto.CommentCreateRequest;
import org.example.mirimilibe.comment.repository.CommentSummaryResponse;
import org.example.mirimilibe.comment.service.CommentService;
import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.member.domain.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	@Operation(summary = "답글 작성", description = "게시글에 답변을 작성합니다.")
	public ResponseEntity<ApiResponse<Long>> createComment(
		@PathVariable Long postId,
		@RequestBody CommentCreateRequest req,
		Member member
	) {
		Long commentId = commentService.createComment(member.getId(), postId, req);
		return ResponseEntity.ok(ApiResponse.success(commentId));
	}

	@GetMapping
	@Operation(summary = "댓글 리스트 조회", description = "게시글에 달린 댓글들을 반환합니다.")
	public ResponseEntity<ApiResponse<List<CommentSummaryResponse>>> getComments(@PathVariable Long postId) {
		List<CommentSummaryResponse> response = commentService.getCommentsByPost(postId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
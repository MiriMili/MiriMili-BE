package org.example.mirimilibe.post.controller;

import java.util.List;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.service.PostScrapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class ScrapPostController {

	private final PostScrapService postScrapService;

	@Operation(
		summary = "게시글 스크랩 또는 스크랩 취소",
		description = "해당 게시글을 스크랩하거나 이미 스크랩된 경우 스크랩을 취소합니다."
	)
	@PostMapping("/{postId}/scrap")
	public ResponseEntity<ApiResponse<Void>> toggleScrap(
		@PathVariable Long postId,
		Member member
	) {
		postScrapService.toggleScrap(member.getId(), postId);
		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@Operation(
		summary = "나의 스크랩 게시글 목록 조회",
		description = "현재 로그인한 사용자가 스크랩한 게시글들을 반환합니다."
	)
	@GetMapping("/scrap/my")
	public ResponseEntity<ApiResponse<List<PostListItemResponse>>> getMyScraps(
		Member member
	) {
		List<PostListItemResponse> response = postScrapService.getMyScrapPosts(member.getId());
		return ResponseEntity.ok(ApiResponse.success(response));
	}



}

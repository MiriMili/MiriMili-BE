package org.example.mirimilibe.post.controller;

import java.util.List;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.service.PostScrapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		postScrapService.toggleScrap(jwtMemberDetail.getMemberId(), postId);
		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@Operation(
		summary = "나의 스크랩 게시글 목록 조회 (페이징)",
		description = "현재 로그인한 사용자가 스크랩한 게시글들을 페이지네이션으로 조회합니다."
	)
	@GetMapping("/scrap/my")
	public ResponseEntity<ApiResponse<CommonPageResponse<PostListItemResponse>>> getMyScraps(
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		CommonPageResponse<PostListItemResponse> response = postScrapService.getMyScrapPosts(jwtMemberDetail.getMemberId(), page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}



}

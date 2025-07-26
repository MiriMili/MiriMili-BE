package org.example.mirimilibe.post.controller;

import java.util.List;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.dto.PostCreateRequest;
import org.example.mirimilibe.post.dto.PostDetailResponse;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.service.PostService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Post API", description = "게시글 관련 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@Operation(
		summary = "게시글 작성",
		description = "게시글을 작성합니다. 제목, 내용, 이미지, 카테고리, 답변자 대상 군구분과 특기를 입력받습니다."
	)
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> createPost(
		@RequestBody PostCreateRequest req,
		Long memberId //  @AuthenticationPrincipal CustomUserDetails user
	) {
		Long postId = postService.createPost(memberId, req);
		return ResponseEntity.ok(ApiResponse.success(postId));
	}


	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(@PathVariable Long postId) {
		PostDetailResponse response = postService.getPostDetail(postId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/list")
	@Operation(summary = "게시글 리스트 조회 (페이징)", description = "선택된 카테고리 중 하나라도 포함된 게시글을 페이징하여 조회합니다.")
	public ResponseEntity<ApiResponse<CommonPageResponse<PostListItemResponse>>> getPostsByCategories(
		@RequestParam List<Long> categoryIds,
		@ParameterObject Pageable pageable
	) {
		CommonPageResponse<PostListItemResponse> response = postService.getPostsByCategory(categoryIds, pageable);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

}
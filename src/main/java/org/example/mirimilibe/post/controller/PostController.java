package org.example.mirimilibe.post.controller;

import java.util.List;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.post.dto.MyActivityResponse;
import org.example.mirimilibe.post.dto.PostCreateRequest;
import org.example.mirimilibe.post.dto.PostDetailResponse;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.dto.PostMyInfoRes;
import org.example.mirimilibe.post.service.PostService;
import org.example.mirimilibe.post.service.RecentSearchService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Post API", description = "게시글 관련 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final RecentSearchService recentSearchService;

	@Operation(
		summary = "게시글 작성",
		description = "게시글을 작성합니다. 제목, 내용, 이미지, 카테고리, 답변자 대상 군구분과 특기를 입력받습니다."
	)
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> createPost(
		@Valid @RequestBody PostCreateRequest req,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {

		Long postId = postService.createPost(jwtMemberDetail.getMemberId(), req);

		return ResponseEntity.ok(ApiResponse.success(postId));
	}


	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(@PathVariable Long postId,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		Long memberId = jwtMemberDetail != null ? jwtMemberDetail.getMemberId() : null;
		PostDetailResponse response = postService.getPostDetail(postId, memberId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/list")
	@Operation(summary = "게시글 리스트 조회 (페이징)", description = "선택된 카테고리 중 하나라도 포함된 게시글을 페이징하여 조회합니다.")
	public ResponseEntity<ApiResponse<CommonPageResponse<PostListItemResponse>>> getPostsByCategories(
		@RequestParam List<Long> categoryIds,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy
	) {
		CommonPageResponse<PostListItemResponse> response = postService.getPostsByCategory(categoryIds, page, size, sortBy);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/search")
	@Operation(summary = "게시글 검색 (제목/본문 포함)", description = "제목 또는 본문에 키워드가 포함된 게시글을 페이징 조회합니다.")
	public ResponseEntity<ApiResponse<CommonPageResponse<PostListItemResponse>>> searchPosts(
		@RequestParam("q") String keyword,
		@ParameterObject
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		CommonPageResponse<PostListItemResponse> response = postService.searchPosts(keyword, pageable);
		// 로그인한 사용자일 경우에만 최근 검색어 저장
		if (jwtMemberDetail != null) {
			recentSearchService.add(jwtMemberDetail.getMemberId(), keyword);
		}
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/answerable")
	@Operation(summary = "답변 가능한 게시물 목록 조회", description = "현재 사용자가 답변할 수 있는 게시물들을 조회합니다.")
	public ResponseEntity<ApiResponse<CommonPageResponse<PostListItemResponse>>> getAnswerablePosts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		CommonPageResponse<PostListItemResponse> response = postService.getAnswerablePosts(jwtMemberDetail.getMemberId(), page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}


	@GetMapping("/{postId}/my")
	@Operation(summary = "답글 작성 가능 여부 및 내 정보 조회", description = "게시글 페이지에서 필요한 내 정보를 반환합니다. <br>"
		+"- isAnswerable: 해당 게시글에 답변을 작성할 수 있는지 여부 ")
	public ResponseEntity<ApiResponse<PostMyInfoRes>> getPostMyInfo(
		@PathVariable Long postId,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		PostMyInfoRes response = postService.getPostMyInfo(jwtMemberDetail.getMemberId(), postId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/activity")
	@Operation(summary = "사이드바에 표시할 정보 (내 활동)")
	public ResponseEntity<ApiResponse<MyActivityResponse>> getMyActivePosts(
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		MyActivityResponse response = postService.getMyActivity(jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{postId}")
	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 작성자만 삭제할 수 있으며, 게시글 삭제 시 해당 게시글의 댓글도 함께 삭제됩니다.")
	public ResponseEntity<ApiResponse<Void>> deletePost(
		@PathVariable Long postId,
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		postService.deletePost(jwtMemberDetail.getMemberId(), postId);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
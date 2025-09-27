package org.example.mirimilibe.post.controller;

import java.util.List;

import org.example.mirimilibe.comment.dto.MyAnswerResponse;
import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.dto.MyPageRes;
import org.example.mirimilibe.member.service.MilitaryInfoService;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

	private final MyPageService myPageService;
	private final MilitaryInfoService militaryInfoService;

	@Operation(summary = "내가 작성한 게시글 목록 조회")
	@GetMapping("/posts")
	public ResponseEntity<ApiResponse<List<PostListItemResponse>>> getMyPosts(
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		List<PostListItemResponse> posts = myPageService.getMyPosts(jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success(posts));
	}

	@Operation(summary = "내가 답변한 댓글 목록 조회")
	@GetMapping("/answers")
	public ResponseEntity<ApiResponse<List<MyAnswerResponse>>> getMyAnswers(
		@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail
	) {
		List<MyAnswerResponse> answers = myPageService.getMyAnswers(jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success(answers));
	}

	@GetMapping("/info")
	@Operation(
		summary = "마이페이지 정보 조회",
		description = "사용자의 기본 정보, 군 정보를 조회하는 API입니다. <br>"
			+ "아직 입력되지 않은 정보는 null로 반환됩니다."
	)
	public ResponseEntity<ApiResponse<MyPageRes>> getProfile(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		MyPageRes res = militaryInfoService.getMilitaryInfo(jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success(res));
	}
}

package org.example.mirimilibe.post.controller;

import java.util.List;

import org.example.mirimilibe.comment.dto.MyAnswerResponse;
import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.service.MyPageService;
import org.springframework.http.ResponseEntity;
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

	@Operation(summary = "내가 작성한 게시글 목록 조회")
	@GetMapping("/posts")
	public ResponseEntity<ApiResponse<List<PostListItemResponse>>> getMyPosts(
		 Member member
	) {
		List<PostListItemResponse> posts = myPageService.getMyPosts(member.getId());
		return ResponseEntity.ok(ApiResponse.success(posts));
	}

	@Operation(summary = "내가 답변한 댓글 목록 조회")
	@GetMapping("/answers")
	public ResponseEntity<ApiResponse<List<MyAnswerResponse>>> getMyAnswers(
		 Member member
	) {
		List<MyAnswerResponse> answers = myPageService.getMyAnswers(member.getId());
		return ResponseEntity.ok(ApiResponse.success(answers));
	}
}

package org.example.mirimilibe.post.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.dto.RecentDtos;
import org.example.mirimilibe.post.service.RecentSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class RecentSearchController {

	private final RecentSearchService recentSearchService;

	@GetMapping("/recent")
	@Operation(summary = "최근 검색어 5개 조회", description = "로그인 사용자의 최근 검색어 5개를 최신순으로 반환합니다.")
	public ResponseEntity<ApiResponse<RecentDtos.RecentRes>> getRecent(Member member) {
		var res = recentSearchService.top5(member != null ? member.getId() : null);
		return ResponseEntity.ok(ApiResponse.success(res));
	}
}

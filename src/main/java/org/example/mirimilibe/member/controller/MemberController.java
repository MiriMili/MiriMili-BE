package org.example.mirimilibe.member.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.member.dto.MilitaryInfoReq;
import org.example.mirimilibe.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@PostMapping("/profile")
	@Operation(
		summary = "프로필 설정",
		description = "사용자의 군 정보를 설정하는 API입니다. "
			+ "MiliType은 ENUM 타입으로, 'ARMY, NAVY, AIR_FORCE' 중 하나를 입력해주세요."
	)
	public ResponseEntity<ApiResponse<String>> updateProfile(@RequestBody MilitaryInfoReq req, @AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		memberService.updateMilitaryInfo(req, jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success("프로필 생성 성공"));
	}

}

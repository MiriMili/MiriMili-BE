package org.example.mirimilibe.member.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.member.dto.MilitaryInfoReq;
import org.example.mirimilibe.member.dto.PwdReq;
import org.example.mirimilibe.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@PostMapping("/profile")
	@Operation(
		summary = "프로필 설정",
		description = "사용자의 군 정보를 설정하는 API입니다. <br>"
			+ "MiliType은 ENUM 타입으로, 'ARMY, NAVY, AIR_FORCE' 중 하나를 입력해주세요. 해당 필드는 필수로 입력해야 합니다."
	)
	public ResponseEntity<ApiResponse<String>> updateProfile(@Valid @RequestBody MilitaryInfoReq req, @AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		memberService.updateMilitaryInfo(req, jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success("프로필 생성 성공"));
	}

	@PatchMapping("/password")
	@Operation(
		summary = "비밀번호 변경",
		description = "회원의 전화번호와 새 비밀번호를 사용하여 비밀번호를 변경합니다. <br>"
			+ "해당 API를 호출하기 전 문자 인증 절차가 선행되어야 합니다."
	)
	public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody PwdReq req) {
		memberService.changePassword(req.phoneNumber(), req.newPassword());
		return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
	}

}

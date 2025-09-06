package org.example.mirimilibe.member.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.member.dto.MilitaryInfoReq;
import org.example.mirimilibe.member.dto.PwdReq;
import org.example.mirimilibe.member.dto.MilitaryInfoRes;
import org.example.mirimilibe.member.dto.MyPageRes;
import org.example.mirimilibe.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
		summary = "상세 프로필 설정/수정",
		description = "현역 사용자의 군 정보를 설정하는 API입니다. (마이페이지: 현역 사용자의 상세 프로필)<br>"
			+ "MiliType은 ENUM 타입으로, 'ARMY, NAVY, AIR_FORCE' 중 하나를 입력해주세요. 해당 필드는 필수로 입력해야 합니다. <br>"
			+ "이미 프로필이 존재하는 경우, 해당 API는 프로필을 수정합니다. <br>"
			+ "- 기존값이 null이었던 필드에 새로운 값을 입력하는 것은 가능합니다. <br>"
			+ "- 기존값이 존재하는 필드에 새로운 값을 입력하는 경우 오류를 반환합니다. <br>"
			+ "- 입대 전 사용자일 경우 오류를 반환합니다. <br>"
			+ "마이페이지에서 호출하실 때는 기존 정보들을 불러오신 후, 수정하실 필드만 변경하여 보내주시면 됩니다. <br>"
	)
	public ResponseEntity<ApiResponse<String>> updateProfile(@Valid @RequestBody MilitaryInfoReq req, @AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		memberService.updateMilitaryInfo(req, jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success("프로필 생성 성공"));
	}

	@PatchMapping("/password")
	@Operation(
		summary = "비밀번호를 잊었을 경우 비밀번호 재설정 처리 (비로그인 상태)",
		description = "회원의 전화번호와 새 비밀번호를 사용하여 비밀번호를 변경합니다. <br>"
			+ "비밀번호를 잊었을 경우 재설정 처리 과정 <br>"
			+ "1. /sms/send-pwd 를 호출하여 전화번호 인증 코드 발송 (인증 코드는 3분 간 유효) <br>"
			+ "2. /sms/verify 를 호출하여 인증 절차 진행 (인증 상태는 5분 간 유효) <br>"
			+ "3. /member/password 를 호출하여 비밀번호 변경 진행"
	)
	public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody PwdReq req) {
		memberService.changePassword(req.phoneNumber(), req.newPassword());
		return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
	}

	@GetMapping("/mypage")
	@Operation(
		summary = "마이페이지 정보 조회",
		description = "사용자의 기본 정보, 군 정보를 조회하는 API입니다. <br>"
			+ "아직 입력되지 않은 정보는 null로 반환됩니다."
	)
	public ResponseEntity<ApiResponse<MyPageRes>> getProfile(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		MyPageRes res = memberService.getMilitaryInfo(jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@PatchMapping("/milistatus")
	@Operation(
		summary = "입대 전 -> 현역 변경",
		description = "입대 전 사용자의 MiliStatus를 현역으로 변경하는 API입니다. <br>"
			+ "요청한 사용자의 현 상태가 PRE_ENLISTED가 아닐 경우 오류를 반환합니다. <br>"
	)
	public ResponseEntity<ApiResponse<String>> changeMiliStatus(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		memberService.updateMiliStatus(jwtMemberDetail.getMemberId());
		return ResponseEntity.ok(ApiResponse.success("군 상태 변경 성공"));
	}

}

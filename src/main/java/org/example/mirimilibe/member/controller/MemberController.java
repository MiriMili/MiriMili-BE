package org.example.mirimilibe.member.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.SignUpReq;
import org.example.mirimilibe.global.auth.service.AuthService;
import org.example.mirimilibe.member.dto.PwdReq;
import org.example.mirimilibe.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final AuthService authService;

	@PostMapping("/signup")
	@Operation(
		summary = "회원가입",
		description = "회원가입 API입니다. 아래 정보를 입력받습니다.<br>"
			+ "- 전화번호: 01011112222 형식, 11자리 숫자 (필수)<br>"
			+ "- 비밀번호: 문자열, 공백 불가 (필수)<br>"
			+ "- 닉네임: 문자열, 공백 불가 (필수)<br>"
			+ "- serviceAgreed, privacyPolicyAgreed, marketingConsentAgreed: 약관 동의 여부, service와 privacy는 true여야 함 (필수)<br>"
			+ "- MiliStatus: ENUM 타입, 'PRE_ENLISTED, ENLISTED, DISCHARGED' 중 하나 (필수)<br>"
			+ "모든 필드는 필수로 입력해야 합니다."
	)
	public ResponseEntity<ApiResponse<?>> signUp(@RequestBody @Valid SignUpReq signUpReq) {
		memberService.signUp(signUpReq);
		return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
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


	@PatchMapping("/delete")
	@Operation(
		summary = "회원 탈퇴",
		description = "로그인한 사용자의 회원 탈퇴를 처리하는 API입니다. <br>"
			+ "회원 탈퇴 시 사용자의 전화번호를 유지하여 재가입을 방지합니다. <br>"
			+ "해당 사용자는 회원 탈퇴 후 로그아웃 처리됩니다. <br>"
	)
	public ResponseEntity<ApiResponse<String>> deleteMember(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail,
		@RequestHeader("Authorization") String authorizationHeader,
		HttpServletResponse response) {
		memberService.deleteMember(jwtMemberDetail.getMemberId());
		authService.logout(jwtMemberDetail.getMemberId(), authorizationHeader, response);
		return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 성공"));
	}

}

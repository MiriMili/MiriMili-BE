package org.example.mirimilibe.global.auth.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.LoginReq;
import org.example.mirimilibe.global.auth.dto.LoginSuccessRes;
import org.example.mirimilibe.global.auth.service.AuthService;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.global.auth.dto.SignUpReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	@Operation(
		summary = "회원가입",
		description = "회원가입을 위한 API입니다. 회원의 전화번호, 비밀번호, 닉네임, 약관 동의를 포함한 정보를 입력받습니다. <br>"
			+ "전화번호는 01011112222 형식으로 11글자 (숫자)로 입력해야 하며, <br>"
			+ "serviceAgreed, privacyPolicyAgreed는 true가 아닐 경우 회원가입이 실패합니다.<br>"
			+ "군 정보는 선택 사항이며, 입력하지 않아도 회원가입이 성공합니다.<br>"
			+ "MiliStatus는 ENUM 타입으로, 'PRE_ENLISTED, ENLISTED, DISCHARGED' 중 하나를 입력해주세요.<br>"
	)
	public ResponseEntity<?> signUp(@RequestBody @Valid SignUpReq signUpReq) {
		authService.signUp(signUpReq);
		return ResponseEntity.ok("회원가입 성공");
	}

	@PostMapping("/login")
	public ResponseEntity<LoginSuccessRes> login(@RequestBody LoginReq loginReq) {
		LoginSuccessRes loginSuccessRes = authService.login(loginReq);
		return ResponseEntity.ok(loginSuccessRes);
	}

	@GetMapping("/checkNickname")
	@Operation(
		summary = "닉네임 중복 검사",
		description = "입력한 닉네임이 이미 사용 중인지 확인하는 API입니다."
	)
	public ResponseEntity<ApiResponse<String>> checkNickname(@RequestParam String nickname) {
		authService.checkDuplicateNickname(nickname);
		return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다."));
	}

	@GetMapping("/test")
	@Operation(	summary = "테스트용 사용자 정보 조회" )
	public ResponseEntity<?> getUserDetail(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		// 테스트용으로 현재 로그인한 사용자의 정보를 반환
		// 실제로는 인증된 사용자 정보를 가져오는 로직이 필요합니다.
		Long userId=jwtMemberDetail.getMemberId();
		String phoneNum=jwtMemberDetail.getUsername();

		return ResponseEntity.ok("userId: " + userId + ", phoneNum: " + phoneNum);
	}
}


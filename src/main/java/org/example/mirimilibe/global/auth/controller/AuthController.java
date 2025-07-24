package org.example.mirimilibe.global.auth.controller;

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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody SignUpReq signUpReq) {
		authService.signUp(signUpReq);
		return ResponseEntity.ok("회원가입 성공");
	}

	@PostMapping("/login")
	public ResponseEntity<LoginSuccessRes> login(@RequestBody LoginReq loginReq) {
		LoginSuccessRes loginSuccessRes = authService.login(loginReq);
		return ResponseEntity.ok(loginSuccessRes);
	}

	@GetMapping("/test")
	public ResponseEntity<?> getUserDetail(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		// 테스트용으로 현재 로그인한 사용자의 정보를 반환
		// 실제로는 인증된 사용자 정보를 가져오는 로직이 필요합니다.
		String phoneNumber = jwtMemberDetail.getPhoneNumber();
		Long userId=jwtMemberDetail.getMemberId();

		return ResponseEntity.ok(Json.pretty(
			Member.builder()
				.number(phoneNumber)
				.id(userId)
				.build()
		));
	}
}


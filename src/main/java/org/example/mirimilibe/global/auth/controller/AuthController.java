package org.example.mirimilibe.global.auth.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.LoginReq;
import org.example.mirimilibe.global.auth.dto.LoginSuccessRes;
import org.example.mirimilibe.global.auth.dto.RefreshDTO;
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
	public ResponseEntity<ApiResponse<?>> signUp(@RequestBody @Valid SignUpReq signUpReq) {
		authService.signUp(signUpReq);
		return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginSuccessRes>> login(@RequestBody LoginReq loginReq) {
		LoginSuccessRes loginSuccessRes = authService.login(loginReq);
		return ResponseEntity.ok(ApiResponse.success(loginSuccessRes));
	}

	@PostMapping("/reissue")
	@Operation(summary = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.",
		description = "리프레시 토큰이 유효한 경우 새로운 액세스 토큰을 발급합니다. "
			+ "액세스 토큰이 만료되어 401 Unauthorized 에러가 발생한 경우에만 사용해야 합니다."
			+ "헤더에 만료된 액세스 토큰을 Bearer 타입으로 포함시켜야 합니다.")
	public ResponseEntity<ApiResponse<RefreshDTO.Res>> reissue(@RequestBody @Valid RefreshDTO.Req req) {
		RefreshDTO.Res res = authService.refreshToken(req);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@GetMapping("/test")
	public ResponseEntity<?> getUserDetail(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail) {
		// 테스트용으로 현재 로그인한 사용자의 정보를 반환
		// 실제로는 인증된 사용자 정보를 가져오는 로직이 필요합니다.
		Long userId=jwtMemberDetail.getMemberId();
		String phoneNum=jwtMemberDetail.getUsername();

		return ResponseEntity.ok("userId: " + userId + ", phoneNum: " + phoneNum);
	}
}


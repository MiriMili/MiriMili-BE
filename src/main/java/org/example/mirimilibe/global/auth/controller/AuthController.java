package org.example.mirimilibe.global.auth.controller;

import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.LoginReq;
import org.example.mirimilibe.global.auth.dto.LoginSuccessRes;
import org.example.mirimilibe.global.auth.dto.RefreshDTO;
import org.example.mirimilibe.global.auth.service.AuthService;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.global.auth.dto.SignUpReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	@Operation(
		summary = "로그인",
		description = "회원의 전화번호와 비밀번호를 사용하여 로그인합니다. <br>"
			+ "로그인 성공 시 액세스 토큰과 리프레시 토큰, 닉네임, 추가정보 여부를 반환합니다.<br>"
			+ "추가 정보 여부가 false일 경우, 추가 정보 입력 페이지로 리다이렉트되어야 합니다.<br>"
		    + "추가 정보 여부는 사용자가 현역이 아닐 경우 true로 반환됩니다."
	)
	public ResponseEntity<ApiResponse<LoginSuccessRes>> login(@RequestBody LoginReq loginReq, HttpServletResponse response) {
		LoginSuccessRes loginSuccessRes = authService.login(loginReq, response);
		return ResponseEntity.ok(ApiResponse.success(loginSuccessRes));
	}

	@PostMapping("/reissue")
	@Operation(summary = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.",
		description = "리프레시 토큰이 유효한 경우 새로운 액세스 토큰을 발급합니다. "
			+ "액세스 토큰이 만료되어 401 Unauthorized 에러가 발생한 경우에만 사용해야 합니다."
			+ "헤더에 만료된 액세스 토큰을 Bearer 타입으로 포함시켜야 합니다.")
	public ResponseEntity<ApiResponse<RefreshDTO.Res>> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken,
		HttpServletResponse response) {
		if(refreshToken==null)
			throw new MiriMiliException(MemberErrorCode.REFRESH_NOT_FOUND);
		RefreshDTO.Res res = authService.refreshToken(refreshToken, response);
		return ResponseEntity.ok(ApiResponse.success(res));
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

	@PatchMapping("/logout")
	@Operation(
		summary = "로그아웃",
		description = "사용자의 리프레시 토큰을 무효화하여 로그아웃 처리하는 API입니다. <br>"
			+ "로그아웃 시 클라이언트 측에서도 액세스 토큰과 리프레시 토큰을 삭제해야 합니다."
	)
	public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal JwtMemberDetail jwtMemberDetail,
		@RequestHeader("Authorization") String authorizationHeader,
		HttpServletResponse response) {
		authService.logout(jwtMemberDetail.getMemberId(), authorizationHeader, response);
		return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
	}
}


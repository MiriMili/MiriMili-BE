package org.example.mirimilibe.global.auth.jwt.handler;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {

		// 1. 응답 상태 및 헤더 설정
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");

		// 2. 응답 메시지 작성
		Map<String, String> errorResponse = Map.of(
			"error", "로그인에 실패했습니다. 전화번호 또는 비밀번호를 확인해주세요."
		);

		// 3. JSON 응답 출력
		objectMapper.writeValue(response.getWriter(), errorResponse);

		// 4. 로그 기록
		log.info("로그인 실패: {}", exception.getMessage());
	}
}

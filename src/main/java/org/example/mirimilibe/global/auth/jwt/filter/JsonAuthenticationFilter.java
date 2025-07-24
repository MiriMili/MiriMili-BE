package org.example.mirimilibe.global.auth.jwt.filter;

import java.io.IOException;

import org.example.mirimilibe.global.auth.jwt.dto.LoginReq;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * JsonAuthenticationFilter는 JSON 형식의 로그인 요청을 처리하는 필터입니다.
 * 로그인 요청은 POST 방식으로 /api/login 엔드포인트로 전송되어야 하며,
 * 요청 본문은 JSON 형식이어야 합니다.
 * username은 사용자의 전화번호이며, password는 사용자의 비밀번호입니다.
 */
public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static final String LOGIN_URL = "/auth/login";
	private static final String CONTENT_TYPE = "application/json";
	private static final String HTTP_METHOD = "POST";

	private final ObjectMapper objectMapper;

	private static final RequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER = request ->
		HTTP_METHOD.equalsIgnoreCase(request.getMethod()) && LOGIN_URL.equals(request.getServletPath());

	public JsonAuthenticationFilter(ObjectMapper objectMapper) {
		super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
		this.objectMapper = objectMapper;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)  ) {
			throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
		}
		// 요청 본문에서 전화번호와 비밀번호를 추출
		LoginReq loginReq = objectMapper.readValue(request.getInputStream(), LoginReq.class);
		String phoneNumber = loginReq.phoneNumber();
		String password = loginReq.password();

		UsernamePasswordAuthenticationToken authRequest  = new UsernamePasswordAuthenticationToken(phoneNumber, password, null);

		// 인증 요청을 처리할 AuthenticationManager에게 전달
		return this.getAuthenticationManager().authenticate(authRequest);
	}
}

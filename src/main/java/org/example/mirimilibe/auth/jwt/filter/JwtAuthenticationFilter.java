package org.example.mirimilibe.auth.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.example.mirimilibe.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenUtil jwtTokenUtil;

	private static final String GRANT_TYPE = "Bearer ";

	protected List<String> filterPassList=List.of(
		"/auth/login",
		"/auth/signup",
		"/webjars/**",
		"/v3/api-docs"
	);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		// JWT 토큰을 추출하고 검증하는 로직을 여기에 추가합니다.

		//1. 요청 URL이 필터링 대상인지 확인
		if(filterPassList.contains(request.getRequestURI()) || request.getRequestURI().contains("swagger")) {
			// 필터링 대상이면 다음 필터로 넘어감
			filterChain.doFilter(request, response);
			return;
		}

		//2. Authorization 헤더에서 JWT 토큰 추출
		String accessToken = jwtTokenUtil.extractAccessToken(request)
			.orElse(null);

		if (accessToken == null) {
			log.warn("Authorization 헤더에 access-token이 없습니다.");
			throw new MiriMiliException(MemberErrorCode.ACCESS_TOKEN_NOT_FOUND);
		}

		//4. 토큰에서 사용자 정보 추출
		Authentication authentication;

		/*
		getAuthentication 메서드는 해당 토큰에 대한 인증 정보를 반환합니다.
		그 과정에서 토큰의 유효성을 검사하고, 토큰이 유효한 경우 사용자 정보를 포함한 Authentication 객체를 생성합니다.
		토큰이 만료됐을 경우 ExpiredJwtException 예외가 발생합니다.
		 */
		try {
			authentication = jwtTokenUtil.getAuthentication(accessToken);
			log.info("인증된 사용자: {}", authentication.getName());

			SecurityContextHolder.getContext().setAuthentication(authentication);
			response.setHeader("Authorization", GRANT_TYPE + accessToken);

			filterChain.doFilter(request, response);

		}catch(ExpiredJwtException e){
			log.error("만료된 access-token: {}", accessToken);
			throw new MiriMiliException(MemberErrorCode.ACCESS_EXPIRED);
			// TODO: 추후 refresh-token 관련 처리 추가

		}catch (Exception e) {
			//6. 토큰이 유효하지 않은 경우 예외 처리
			log.error("유효하지 않은 access-token: {}", accessToken, e);
			throw new MiriMiliException(MemberErrorCode.ACCESS_FORBIDDEN);
		}


	}
}

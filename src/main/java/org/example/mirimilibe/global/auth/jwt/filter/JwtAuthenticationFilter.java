package org.example.mirimilibe.global.auth.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.example.mirimilibe.global.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.global.auth.service.BlackListService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
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
	private final BlackListService blackListService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	protected List<String> filterPassList=List.of(
		"/auth/login",
		"/actuator/prometheus",
		"/member/signup",
		"/auth/checkNickname",
		"/swagger-ui/**",
		"/swagger-resources/**",
		"/v3/api-docs/**",
		"/sms/verify",
		"/sms/send",
		"/sms/send-pwd",
		"/member/password",
		"/posts/list",
		"/posts/search"
	);

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		//1. 필터링 제외 경로 설정
		String requestURI = request.getRequestURI();
		return filterPassList.stream().anyMatch(pattern->pathMatcher.match(pattern, requestURI));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		//2. Authorization 헤더에서 JWT 토큰 추출
		String accessToken = jwtTokenUtil.extractAccessToken(request)
			.orElse(null);

		if (accessToken == null) {
			log.warn("Authorization 헤더에 access-token이 없습니다.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		if (blackListService.isBlacklisted(accessToken)) {
			log.warn("블랙리스트에 등록된 access-token 요청입니다.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		//4. 토큰에서 사용자 정보 추출
		Authentication authentication;

		//5. SecurityContext에 인증 정보 저장
		try {
			authentication = jwtTokenUtil.getAuthentication(accessToken);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			filterChain.doFilter(request, response);

		}catch(ExpiredJwtException e){
			String requestURI = request.getRequestURI();
			log.info("[만료된 access-token 요청] 요청 URI: {}", requestURI);

			if (requestURI.equals("/auth/reissue")) {
				filterChain.doFilter(request, response);
				return;
			}
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		}catch (Exception e) {
			//6. 토큰이 유효하지 않은 경우 예외 처리
			log.error("유효하지 않은 access-token: {}", accessToken, e);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}

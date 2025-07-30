package org.example.mirimilibe.global.auth.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.example.mirimilibe.global.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

	private static final String GRANT_TYPE = "Bearer ";
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	protected List<String> filterPassList=List.of(
		"/auth/login",
		"/auth/signup",
		"/swagger-ui/**",
		"/swagger-resources/**",
		"/v3/api-docs/**"
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
			//throw new MiriMiliException(MemberErrorCode.ACCESS_TOKEN_NOT_FOUND);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json;charset=UTF-8");
			return;
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
			log.warn("만료된 access-token: {}", accessToken);

			// TODO: 추후 refresh-token 관련 처리 추가
			String requestURI = request.getRequestURI();
			log.info("[만료된 access-token 요청] 요청 URI: {}", requestURI);

			if (requestURI.equals("/auth/reissue")) {
				filterChain.doFilter(request, response);
				return;
			}
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");

		}catch (Exception e) {
			//6. 토큰이 유효하지 않은 경우 예외 처리
			log.error("유효하지 않은 access-token: {}", accessToken, e);
			throw new MiriMiliException(MemberErrorCode.ACCESS_FORBIDDEN);
		}


	}
}

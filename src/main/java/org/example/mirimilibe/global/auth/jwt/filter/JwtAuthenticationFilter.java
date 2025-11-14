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


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String accessToken = jwtTokenUtil.extractAccessToken(request)
			.orElse(null);

		try {
			if(accessToken!=null){
				if (blackListService.isBlacklisted(accessToken)) {
					log.warn("블랙리스트에 등록된 access-token 요청입니다.");
					filterChain.doFilter(request, response);
					return;
				}

				Authentication authentication = jwtTokenUtil.getAuthentication(accessToken);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

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
			log.error("유효하지 않은 access-token: {}", accessToken, e);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}

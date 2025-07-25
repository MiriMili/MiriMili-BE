package org.example.mirimilibe.global.auth.jwt.util;

import java.util.Optional;

import org.example.mirimilibe.member.domain.Member;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtTokenUtil {

	String generateAccessToken(Authentication authentication);

	String generateRefreshToken();

	boolean validateToken(String token);

	Authentication getAuthentication(String token);

	Authentication createAuthentication(Member member);

	Optional<String> extractAccessToken(HttpServletRequest request);

	Optional<String> extractId(String token);

}

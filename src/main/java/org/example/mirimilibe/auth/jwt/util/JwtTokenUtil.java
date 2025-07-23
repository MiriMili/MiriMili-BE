package org.example.mirimilibe.auth.jwt.util;

import java.util.Optional;

import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtTokenUtil {

	String generateAccessToken(Authentication authentication);

	String generateRefreshToken();

	boolean validateToken(String token);

	Optional<String> extractAccessToken(HttpServletRequest request);

	Authentication getAuthentication(String token);

	Optional<String> extractId(String token);

	Optional<String> extractPhoneNumber(String token);
}

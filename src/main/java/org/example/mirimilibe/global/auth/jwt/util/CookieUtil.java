package org.example.mirimilibe.global.auth.jwt.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

	public void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.maxAge(maxAge)
			.sameSite("None")
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void deleteCookie(HttpServletResponse response, String name) {
		Cookie cookie = new Cookie(name, null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}

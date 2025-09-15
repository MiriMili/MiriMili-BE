package org.example.mirimilibe.global.auth.service;

import java.util.Date;

import org.example.mirimilibe.global.auth.jwt.util.JwtTokenUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlackListService {

	private final JwtTokenUtil jwtTokenProvider;
	private final String BLACKLIST_KEY = "blacklist:";
	private final StringRedisTemplate stringRedisTemplate;

	public void addBlacklist(String token) {
		Date expiration = jwtTokenProvider.extractExpiration(token)
			.orElseThrow(() -> new IllegalArgumentException("토큰에서 만료 시간을 추출할 수 없습니다."));

		String key = BLACKLIST_KEY + token;
		long now = new Date().getTime();
		long remainingExpiration = expiration.getTime() - now;

		if (remainingExpiration > 0) { // 남은 만료시간만큼 블랙리스트로 등록
			stringRedisTemplate.opsForValue().set(key, "logout", remainingExpiration);
		}

	}

	public boolean isBlacklisted(String token) {
		String key =  BLACKLIST_KEY + token;
		return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
	}
}

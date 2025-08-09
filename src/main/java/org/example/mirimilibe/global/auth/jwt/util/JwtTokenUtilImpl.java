package org.example.mirimilibe.global.auth.jwt.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenUtilImpl implements JwtTokenUtil {

	private final SecretKey secretKey;
	private final MemberRepository memberRepository;

	@Value("${spring.jwt.token.expire.access}")
	private Long accessExpiration;
	@Value("${spring.jwt.token.expire.refresh}")
	private Long refreshExpiration;

	private static final String GRANT_TYPE="Bearer ";

	public JwtTokenUtilImpl(@Value("${spring.jwt.token.secret-key}") String secretKey,
		MemberRepository memberRepository) {
		byte[] keyBytes= Decoders.BASE64.decode(secretKey); // Base64로 인코딩된 비밀 키를 디코딩
		this.secretKey= Keys.hmacShaKeyFor(keyBytes); // 바이트 배열을 HMAC SHA 알고리즘을 사용하여 비밀 키 생성
		this.memberRepository = memberRepository;
	}

	@Override
	public String generateAccessToken(Authentication authentication) {
		//1. 인증 정보에서 사용자 ID 추출
		JwtMemberDetail jwtMemberDetail = (JwtMemberDetail) authentication.getPrincipal();

		//2. JWT 토큰 생성
		String accessToken= Jwts.builder()
			.claim("id", jwtMemberDetail.getMemberId())
			.signWith(secretKey)
			.expiration(Date.from(Instant.now().plusMillis(accessExpiration * 1000)))
			.compact();

		//3. 액세스 토큰 반환
		return GRANT_TYPE + accessToken;
	}

	@Override
	public String generateRefreshToken(Authentication authentication) {
		JwtMemberDetail jwtMemberDetail = (JwtMemberDetail) authentication.getPrincipal();

		return Jwts.builder()
			.claim("id", jwtMemberDetail.getMemberId())
			.signWith(secretKey)
			.expiration(Date.from(Instant.now().plusMillis(refreshExpiration * 1000)))
			.compact();
	}

	@Override
	public Authentication getAuthentication(String token) {

		//1. 사용자 ID 추출, Member 객체 조회
		Long userId = extractId(token)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Member member=memberRepository.findById(userId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

		//2. 사용자 정보 클래스 생성
		JwtMemberDetail jwtMemberDetail = JwtMemberDetail.jwtMemberDetailBuilder()
			.authorities(authorities)
			.memberId(member.getId())
			.username(member.getNumber())
			.password(member.getPassword())
			.build();

		return new UsernamePasswordAuthenticationToken(jwtMemberDetail, member.getPassword(), authorities);
	}

	@Override
	public Authentication createAuthentication(Member member){
		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

		//2. 사용자 정보 클래스 생성
		JwtMemberDetail jwtMemberDetail = JwtMemberDetail.jwtMemberDetailBuilder()
			.authorities(authorities)
			.memberId(member.getId())
			.username(member.getNumber())
			.password(member.getPassword())
			.build();

		return new UsernamePasswordAuthenticationToken(jwtMemberDetail, member.getPassword(), authorities);
	}

	@Override
	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader("Authorization"))
			.filter(token ->
				token.startsWith(GRANT_TYPE))
			.map(token ->
				token.replace(GRANT_TYPE, ""));
	}

	@Override
	public Optional<Long> extractId(String token) {
		return Optional.ofNullable(extractClaims(token).get("id", Long.class));
	}

	@Override
	public boolean validateToken(String token) {
		try{
			Jwts.parser()
				.verifyWith(secretKey)      // 서명 키
				.build()
				.parseSignedClaims(token);   // 서명 검증 + 파싱
			return true;
		}catch(Exception e){
			log.error("토큰 유효성 검사 실패: {}", e.getMessage());
		}
		return false;
	}


	private Claims extractClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}


}

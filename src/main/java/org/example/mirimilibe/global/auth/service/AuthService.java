package org.example.mirimilibe.global.auth.service;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.Status;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.LoginReq;
import org.example.mirimilibe.global.auth.dto.LoginSuccessRes;
import org.example.mirimilibe.global.auth.dto.RefreshDTO;
import org.example.mirimilibe.global.auth.jwt.util.CookieUtil;
import org.example.mirimilibe.global.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.MilitaryInfoErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final MemberRepository memberRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final MilitaryInfoRepository militaryInfoRepository;
	private final BlackListService blackListService;
	private final CookieUtil cookieUtil;

	@Transactional
	public LoginSuccessRes login(LoginReq loginReq, HttpServletResponse response) {
		// 0. 전화번호로 회원 조회
		Member member = memberRepository.findByNumber(loginReq.phoneNumber())
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		if(member.getStatus() == Status.INACTIVE) {
			throw new MiriMiliException(MemberErrorCode.ACCESS_FORBIDDEN);
		}

		// 1. 인증 시도
		Authentication authentication;

		try {
			authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginReq.phoneNumber(), loginReq.password())
			);
		} catch (BadCredentialsException e) {
			throw new MiriMiliException(MemberErrorCode.PASSWORD_MISMATCH);
		}

		// 2. 인증된 사용자 정보 추출
		JwtMemberDetail userDetails = (JwtMemberDetail)authentication.getPrincipal();

		// 3. JWT 생성
		Authentication newAuth = jwtTokenUtil.createAuthentication(member);
		String accessToken = jwtTokenUtil.generateAccessToken(newAuth);
		String refreshToken = jwtTokenUtil.generateRefreshToken(newAuth);
		long refreshTokenExpiry = jwtTokenUtil.extractExpiration(refreshToken)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.REFRESH_EXPIRED));

		// 4. 로그인 성공 로그
		log.info("로그인 성공: 전화번호={}, 사용자 ID={}", loginReq.phoneNumber(), member.getId());
		member.updateRefreshToken(refreshToken);

		//4-1. 현역 여부 및 군 정보 초기화 여부 확인
		boolean isMilitaryInfoInit = checkMilitaryInfoInit(userDetails.getMemberId());

		// 5. 결과 반환, 리프레시 토큰 쿠키 할당
		cookieUtil.setCookie(response, "refreshToken", refreshToken, (int) (refreshTokenExpiry/1000));

		return LoginSuccessRes.of(accessToken, member.getNickname(), isMilitaryInfoInit);

	}

	@Transactional
	public RefreshDTO.Res refreshToken(String refreshToken, HttpServletResponse response) {
		if( !jwtTokenUtil.validateToken(refreshToken)) {
			log.warn("유효하지 않은 리프레시 토큰: {}", refreshToken);
			throw new MiriMiliException(MemberErrorCode.REFRESH_EXPIRED);
		}

		Long memberId = jwtTokenUtil.extractId(refreshToken)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		if (!member.getRefreshToken().equals(refreshToken)) {
			throw new MiriMiliException(MemberErrorCode.INVALID_MEMBER_PARAMETER);
		}

		// 재발급
		Authentication authentication = jwtTokenUtil.createAuthentication(member);
		String newAccessToken = jwtTokenUtil.generateAccessToken(authentication);
		String newRefreshToken = jwtTokenUtil.generateRefreshToken(authentication);
		long refreshTokenExpiry = jwtTokenUtil.extractExpiration(newRefreshToken)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.REFRESH_EXPIRED));

		member.updateRefreshToken(newRefreshToken);
		cookieUtil.setCookie(response, "refreshToken", newRefreshToken, (int) (refreshTokenExpiry/1000));

		log.info("리프레시 토큰 성공: 전화번호={}, 사용자 ID={}", member.getNickname(), member.getId());

		return RefreshDTO.Res.of(newAccessToken);
	}

	@Transactional
	public void logout(Long memberId, String accessToken, HttpServletResponse response) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		member.updateRefreshToken(null);
		cookieUtil.deleteCookie(response, "refreshToken");

		accessToken = accessToken.replace("Bearer ", "").trim();
		blackListService.addBlacklist(accessToken);

		log.info("로그아웃 성공: 사용자 ID={}", memberId);
	}

	public void checkDuplicateNickname(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new MiriMiliException(MemberErrorCode.DUPLICATE_NICKNAME);
		}
	}

	private boolean checkMilitaryInfoInit(Long memberId) {
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		if (militaryInfo.getMiliStatus().equals(MiliStatus.ENLISTED)) {
			return militaryInfo.getMiliType() != null;
		}

		return true;
	}
}

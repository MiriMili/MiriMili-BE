package org.example.mirimilibe.global.auth.service;

import org.example.mirimilibe.global.auth.dto.LoginReq;
import org.example.mirimilibe.global.auth.dto.LoginSuccessRes;
import org.example.mirimilibe.global.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.global.auth.dto.SignUpReq;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final MemberRepository memberRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;

	public void signUp(SignUpReq signUpReq) {
		//1. 유효성 검사
		//1-1. 전화번호 중복 검사
		//1-2. 닉네임 중복 검사

		//2. 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(signUpReq.password());

		Member member= Member.builder()
			.number(signUpReq.phoneNumber())
			.password(encodedPassword)
			.nickname(signUpReq.nickname())
			.build();

		//3. 회원 정보 저장
		memberRepository.save(member);
	}

	@Transactional(readOnly = true)
	public LoginSuccessRes login(LoginReq loginReq) {
		try {
			// 1. 인증 시도
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginReq.phoneNumber(),
					loginReq.password()
				)
			);

			// 2. 인증된 사용자 정보 추출
			UserDetails userDetails = (UserDetails)authentication.getPrincipal();
			Long memberId = Long.valueOf(userDetails.getUsername());

			Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

			// 3. JWT 생성
			Authentication newAuth = jwtTokenUtil.createAuthentication(member);
			String accessToken = jwtTokenUtil.generateAccessToken(newAuth);

			// 4. 결과 반환
			return LoginSuccessRes.of(accessToken, member.getNickname());
		}
		catch (Exception e) {
			// 인증 실패 시 예외 처리
			throw new MiriMiliException(MemberErrorCode.INVALID_MEMBER_PARAMETER);
		}
	}

}

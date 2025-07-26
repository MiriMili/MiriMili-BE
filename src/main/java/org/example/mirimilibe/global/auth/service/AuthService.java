package org.example.mirimilibe.global.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.example.mirimilibe.common.Enum.Status;
import org.example.mirimilibe.common.Enum.TermType;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.auth.dto.LoginReq;
import org.example.mirimilibe.global.auth.dto.LoginSuccessRes;
import org.example.mirimilibe.global.auth.jwt.util.JwtTokenUtil;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.global.auth.dto.SignUpReq;
import org.example.mirimilibe.member.domain.MemberTerm;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MemberTermRepository;
import org.example.mirimilibe.member.service.MemberService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final MemberRepository memberRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final MemberService memberService;
	private final MemberTermRepository memberTermRepository;

	public void signUp(SignUpReq signUpReq) {
		//1. 약관 동의 검사
		if (!signUpReq.serviceAgreed() || !signUpReq.privacyPolicyAgreed()) {
			throw new MiriMiliException(MemberErrorCode.INVALID_MEMBER_PARAMETER);
		}

		checkDuplicatePhoneNumber(signUpReq.phoneNumber());
		checkDuplicateNickname(signUpReq.nickname());

		//2. 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(signUpReq.password());

		LocalDateTime now = LocalDateTime.now();

		Member member = Member.builder()
			.number(signUpReq.phoneNumber())
			.password(encodedPassword)
			.nickname(signUpReq.nickname())
			.status(Status.ACTIVE)
			.createdAt(now)
			.build();

		//3. 회원 정보 저장
		memberRepository.save(member);

		//4. 약관 동의 정보 저장
		List<MemberTerm> memberTerms = Stream.of(
				TermType.SERVICE,
				TermType.PRIVACY,
				signUpReq.marketingConsentAgreed() ? TermType.MARKETING : null)
			.filter(Objects::nonNull)
			.map(type -> MemberTerm.builder()
				.member(member)
				.termType(type)
				.agreedAt(now)
				.build())
			.toList();

		memberTermRepository.saveAll(memberTerms);

		//5. 군 정보 저장
		memberService.updateMilitaryInfo(signUpReq.militaryInfoReq(), member);

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
			JwtMemberDetail userDetails = (JwtMemberDetail)authentication.getPrincipal();
			Long memberId = userDetails.getMemberId();

			Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

			// 3. JWT 생성
			Authentication newAuth = jwtTokenUtil.createAuthentication(member);
			String accessToken = jwtTokenUtil.generateAccessToken(newAuth);

			// 4. 로그인 성공 로그
			log.info("로그인 성공: 전화번호={}, 사용자 ID={}", loginReq.phoneNumber(), member.getId());

			// 5. 결과 반환
			return LoginSuccessRes.of(accessToken, member.getNickname());
		}
		catch (Exception e) {
			// 인증 실패 시 예외 처리
			log.warn("로그인 실패: 전화번호={}, {}", loginReq.phoneNumber(), e.getMessage());
			throw new MiriMiliException(MemberErrorCode.INVALID_MEMBER_PARAMETER);
		}
	}

	public void checkDuplicatePhoneNumber(String phoneNumber) {
		if (memberRepository.existsByNumber(phoneNumber)) {
			throw new MiriMiliException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
		}
	}

	public void checkDuplicateNickname(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new MiriMiliException(MemberErrorCode.DUPLICATE_NICKNAME);
		}
	}

}

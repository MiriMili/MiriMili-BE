package org.example.mirimilibe.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


import org.example.mirimilibe.common.Enum.Status;
import org.example.mirimilibe.common.Enum.TermType;

import org.example.mirimilibe.global.auth.dto.SignUpReq;
import org.example.mirimilibe.global.auth.service.CoolSmsService;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.SmsErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MemberTerm;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MemberTermRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final CoolSmsService coolSmsService;
	private final MemberTermRepository memberTermRepository;
	private final MilitaryInfoService militaryInfoService;


	public void signUp(SignUpReq signUpReq) {
		//0. 문자 인증 여부 조회
		/*if( !coolSmsService.isCertificationCompleted(signUpReq.phoneNumber()) ) {
			throw new MiriMiliException(SmsErrorCode.NEED_SMS_VERIFICATION);
		}*/

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

		//5. 군 정보 생성
		militaryInfoService.createMilitaryInfo(signUpReq.miliStatus(), member);

	}

	public void changePassword(String phoneNumber, String newPassword) {
		// 0. 문자 인증 완료 여부 확인
		if (!coolSmsService.isCertificationCompleted(phoneNumber)) {
			throw new MiriMiliException(SmsErrorCode.NEED_SMS_VERIFICATION);
		}

		// 1. 전화번호로 회원 조회
		Member member = memberRepository.findByNumber(phoneNumber)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		// 2. 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(newPassword);

		// 3. 비밀번호 업데이트
		member.updatePassword(encodedPassword);

		// 4. 회원 정보 저장
		memberRepository.save(member);
	}


	public void deleteMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		member.deleteMember();

		memberRepository.save(member);
		log.info("회원 탈퇴: 사용자 ID={}", memberId);
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

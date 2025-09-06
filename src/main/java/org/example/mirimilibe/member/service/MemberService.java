package org.example.mirimilibe.member.service;

import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;
import org.example.mirimilibe.global.auth.service.CoolSmsService;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.SmsErrorCode;
import org.example.mirimilibe.global.error.MilitaryInfoErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.dto.MilitaryInfoReq;
import org.example.mirimilibe.member.dto.MilitaryInfoRes;
import org.example.mirimilibe.member.dto.MyPageRes;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.example.mirimilibe.member.repository.UnitRepository;
import org.example.mirimilibe.post.repository.SpecialtyRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final SpecialtyRepository specialtyRepository;
	private final UnitRepository unitRepository;
	private final MilitaryInfoRepository militaryInfoRepository;
	private final StringRedisTemplate stringRedisTemplate;
	private final BCryptPasswordEncoder passwordEncoder;
	private final CoolSmsService coolSmsService;


	public void createMilitaryInfo(MiliStatus miliStatus, Member member) {
		// 1. MilitaryInfo가 이미 존재하는지 확인
		if (militaryInfoRepository.existsByMemberId(member.getId())) {
			throw new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_ALREADY_EXISTS);
		}

		// 2. MilitaryInfo 객체 생성
		MilitaryInfo militaryInfo=MilitaryInfo.builder()
			.member(member)
			.miliStatus(miliStatus)
			.build();

		// 3. MilitaryInfo 저장
		militaryInfoRepository.save(militaryInfo);
	}

	public void updateMilitaryInfo(MilitaryInfoReq militaryInfoReq, Long memberId) {
		// 1. MilitaryInfo 객체 조회
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		if(militaryInfo.getMiliStatus() == MiliStatus.PRE_ENLISTED) {
			throw new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_CANNOT_ACCESS);
		}

		Specialty specialty = Optional.ofNullable(militaryInfoReq.specialtyId())
			.flatMap(specialtyRepository::findById)
			.orElse(null);

		Unit unit = Optional.ofNullable(militaryInfoReq.unitId())
			.flatMap(unitRepository::findById)
			.orElse(null);

		// 2. MilitaryInfoReq를 MilitaryInfo에 적용
		applyImmutableFields(militaryInfo, militaryInfoReq, specialty, unit);

		// 3. MilitaryInfo 저장
		militaryInfoRepository.save(militaryInfo);
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

	public MyPageRes getMilitaryInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		MilitaryInfoRes militaryInfoRes=MilitaryInfoRes.fromEntity(militaryInfo);

		return MyPageRes.of(militaryInfo.getMiliStatus(), member.getNickname(), member.getNumber(), militaryInfoRes);
	}

	public void updateMiliStatus(Long memberId) {
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		if(militaryInfo.getMiliStatus() != MiliStatus.PRE_ENLISTED) {
			throw new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}

		militaryInfo.setMiliStatus(MiliStatus.ENLISTED);

		militaryInfoRepository.save(militaryInfo);
	}

	public void applyImmutableFields(MilitaryInfo info, MilitaryInfoReq req, Specialty specialty, Unit unit) {
		miliInfoValidateAndSet(info.getMiliType(), req.type(), info::setMiliType);
		miliInfoValidateAndSet(info.getSpecialty(), specialty, info::setSpecialty);
		miliInfoValidateAndSet(info.getUnit(), unit, info::setUnit);
		miliInfoValidateAndSet(info.getStartDate(), req.startDate(), info::setStartDate);
		miliInfoValidateAndSet(info.getPrivateDate(), req.privateDate(), info::setPrivateDate);
		miliInfoValidateAndSet(info.getCorporalDate(), req.corporalDate(), info::setCorporalDate);
		miliInfoValidateAndSet(info.getSergeantDate(), req.sergeantDate(), info::setSergeantDate);
		miliInfoValidateAndSet(info.getDischargeDate(), req.dischargeDate(), info::setDischargeDate);
	}

	private <T> void miliInfoValidateAndSet(T currentValue, T newValue, Consumer<T> setter) {
		if (newValue == null) {
			return;
		}
		if (currentValue == null) {
			setter.accept(newValue);
		} else if (!currentValue.equals(newValue)) {
			throw new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
	}

}

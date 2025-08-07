package org.example.mirimilibe.member.service;

import java.util.Optional;
import java.util.function.Consumer;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.dto.MilitaryInfoReq;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.example.mirimilibe.member.repository.UnitRepository;
import org.example.mirimilibe.post.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final SpecialtyRepository specialtyRepository;
	private final UnitRepository unitRepository;
	private final MilitaryInfoRepository militaryInfoRepository;

	public void createMilitaryInfo(MiliStatus miliStatus, Member member) {
		// 1. MilitaryInfo가 이미 존재하는지 확인
		if (militaryInfoRepository.existsByMemberId(member.getId())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_ALREADY_EXISTS);
		}

		// 2. MilitaryInfo 객체 생성
		MilitaryInfo militaryInfo = new MilitaryInfo();
		militaryInfo.setMember(member);
		militaryInfo.setMiliStatus(miliStatus);

		// 3. MilitaryInfo 저장
		militaryInfoRepository.save(militaryInfo);
	}

	public void updateMilitaryInfo(MilitaryInfoReq militaryInfoReq, Long memberId) {
		// 1. MilitaryInfo 객체 조회
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MILITARY_INFO_NOT_FOUND));

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

	public void applyImmutableFields(MilitaryInfo info, MilitaryInfoReq req, Specialty specialty, Unit unit) {
		miliInfoValidateAndSet(info.getMiliType(), req.type(), info::setMiliType);
		miliInfoValidateAndSet(info.getMiliStatus(), req.status(), info::setMiliStatus);
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
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
	}

}

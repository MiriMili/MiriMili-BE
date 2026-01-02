package org.example.mirimilibe.member.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;
import org.example.mirimilibe.global.error.MemberErrorCode;
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
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilitaryInfoService {
	private final MemberRepository memberRepository;
	private final MilitaryInfoRepository militaryInfoRepository;
	private final SpecialtyRepository specialtyRepository;
	private final UnitRepository unitRepository;
	private final MiliStatusService miliStatusService;


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
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		if(militaryInfo.getMiliStatus() == MiliStatus.PRE_ENLISTED) {
			throw new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_CANNOT_ACCESS);
		}

		Specialty specialty = null;
		if(militaryInfoReq.specialty() != null) {
			specialty = specialtyRepository.findByValue(militaryInfoReq.specialty())
				.orElseThrow(() -> new MiriMiliException(MemberErrorCode.SPECIALTY_NOT_FOUND));
		}

		Unit unit = null;
		if(militaryInfoReq.unit() != null) {
			unit = unitRepository.findByValue(militaryInfoReq.unit())
				.orElseThrow(() -> new MiriMiliException(MemberErrorCode.UNIT_NOT_FOUND));
		}

		applyImmutableBaseFields(militaryInfo, militaryInfoReq, specialty, unit);
		applyDates(militaryInfo, militaryInfoReq);

		militaryInfoRepository.save(militaryInfo);
	}


	public MyPageRes getMilitaryInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		miliStatusService.updateMiliStatusToDischarged(militaryInfo);

		MilitaryInfoRes militaryInfoRes=MilitaryInfoRes.fromEntity(militaryInfo);

		return MyPageRes.of(militaryInfo.getMiliStatus(), member.getNickname(), member.getNumber(), militaryInfoRes);
	}

	public void updateMiliStatus(Long memberId) {
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		miliStatusService.updateMiliStatusToEnlisted(militaryInfo);

		militaryInfoRepository.save(militaryInfo);
	}

	public void applyImmutableBaseFields(MilitaryInfo info, MilitaryInfoReq req, Specialty specialty, Unit unit) {
		miliInfoValidateAndSet(info.getMiliType(), req.type(), info::setMiliType);
		miliInfoValidateAndSet(info.getSpecialty(), specialty, info::setSpecialty);
		miliInfoValidateAndSet(info.getUnit(), unit, info::setUnit);
		miliInfoValidateAndSet(info.getStartDate(), req.startDate(), info::setStartDate);
	}

	public void applyDates(MilitaryInfo info, MilitaryInfoReq req) {
		if(info.getMiliStatus()==MiliStatus.ENLISTED){
			overwriteIfPresent(req.privateDate(), info::setPrivateDate);
			overwriteIfPresent(req.corporalDate(), info::setCorporalDate);
			overwriteIfPresent(req.sergeantDate(), info::setSergeantDate);
			overwriteIfPresent(req.dischargeDate(), info::setDischargeDate);
		}
		else{
			miliInfoValidateAndSet(info.getPrivateDate(), req.privateDate(), info::setPrivateDate);
			miliInfoValidateAndSet(info.getCorporalDate(), req.corporalDate(), info::setCorporalDate);
			miliInfoValidateAndSet(info.getSergeantDate(), req.sergeantDate(), info::setSergeantDate);
			miliInfoValidateAndSet(info.getDischargeDate(), req.dischargeDate(), info::setDischargeDate);
		}
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

	private void overwriteIfPresent(LocalDate newValue, Consumer<LocalDate> setter) {
		if (newValue == null) {
			return;
		}
		setter.accept(newValue);
	}
}

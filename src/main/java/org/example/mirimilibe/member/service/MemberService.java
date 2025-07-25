package org.example.mirimilibe.member.service;

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

	public void updateMilitaryInfo(MilitaryInfoReq militaryInfoReq, Member member) {
		// 1. MilitaryInfo 객체 생성
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(member.getId())
			.orElseGet(() -> {
				MilitaryInfo newInfo = new MilitaryInfo();
				newInfo.setMember(member);
				return newInfo;
			});

		if(militaryInfoReq == null) {
			militaryInfoRepository.save(militaryInfo);
			return;
		}

		Specialty specialty=specialtyRepository.findById(militaryInfoReq.specialtyId()).orElse(null);
		Unit unit=unitRepository.findById(militaryInfoReq.unitId()).orElse(null);

		// 2. MilitaryInfoReq를 MilitaryInfo에 적용
		applyImmutableFields(militaryInfo, militaryInfoReq, specialty, unit);

		// 3. MilitaryInfo 저장
		militaryInfoRepository.save(militaryInfo);
	}

	public void applyImmutableFields(MilitaryInfo info, MilitaryInfoReq req, Specialty specialty, Unit unit) {
		// 필드 별 비교 및 설정
		// 기존 값이 null인 경우에만 설정, 기존값이 null이 아니고 기존값과 다른 경우 예외 발생
		if (info.getMiliType() == null) {
			info.setMiliType(req.type());
		} else if (!info.getMiliType().equals(req.type())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getMiliStatus() == null) {
			info.setMiliStatus(req.status());
		} else if (!info.getMiliStatus().equals(req.status())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getSpecialty() == null) {
			info.setSpecialty(specialty);
		} else if (!info.getSpecialty().equals(specialty)) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getUnit() == null) {
			info.setUnit(unit);
		} else if (!info.getUnit().equals(unit)) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getStartDate() == null) {
			info.setStartDate(req.startDate());
		} else if (!info.getStartDate().equals(req.startDate())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getPrivateDate() == null) {
			info.setPrivateDate(req.privateDate());
		} else if (!info.getPrivateDate().equals(req.privateDate())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getCorporalDate() == null) {
			info.setCorporalDate(req.corporalDate());
		} else if (!info.getCorporalDate().equals(req.corporalDate())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getSergeantDate() == null) {
			info.setSergeantDate(req.sergeantDate());
		} else if (!info.getSergeantDate().equals(req.sergeantDate())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
		if (info.getDischargeDate() == null) {
			info.setDischargeDate(req.dischargeDate());
		} else if (!info.getDischargeDate().equals(req.dischargeDate())) {
			throw new MiriMiliException(MemberErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}
	}

}

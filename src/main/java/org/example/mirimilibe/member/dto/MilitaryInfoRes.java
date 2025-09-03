package org.example.mirimilibe.member.dto;

import java.time.LocalDate;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.member.domain.MilitaryInfo;

public record MilitaryInfoRes(
	MiliStatus status,
	MiliType type,
	Long specialtyId,
	Long unitId,
	LocalDate startDate,
	LocalDate privateDate,
	LocalDate corporalDate,
	LocalDate sergeantDate,
	LocalDate dischargeDate
) {
	public static MilitaryInfoRes fromEntity(MilitaryInfo militaryInfo){
		return new MilitaryInfoRes(
			militaryInfo.getMiliStatus(),
			militaryInfo.getMiliType(),
			militaryInfo.getSpecialty()!=null ? militaryInfo.getSpecialty().getId() : null,
			militaryInfo.getUnit()!=null ? militaryInfo.getUnit().getId() : null,
			militaryInfo.getStartDate(),
			militaryInfo.getPrivateDate(),
			militaryInfo.getCorporalDate(),
			militaryInfo.getSergeantDate(),
			militaryInfo.getDischargeDate()
		);
	}
}

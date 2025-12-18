package org.example.mirimilibe.member.dto;

import java.time.LocalDate;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.member.domain.MilitaryInfo;

public record MilitaryInfoRes(
	MiliType type,
	String specialtyId,
	String unitId,
	LocalDate startDate,
	LocalDate privateDate,
	LocalDate corporalDate,
	LocalDate sergeantDate,
	LocalDate dischargeDate
) {
	public static MilitaryInfoRes fromEntity(MilitaryInfo militaryInfo){
		return new MilitaryInfoRes(
			militaryInfo.getMiliType(),
			militaryInfo.getSpecialty()!=null ? militaryInfo.getSpecialty().getValue() : null,
			militaryInfo.getUnit()!=null ? militaryInfo.getUnit().getValue() : null,
			militaryInfo.getStartDate(),
			militaryInfo.getPrivateDate(),
			militaryInfo.getCorporalDate(),
			militaryInfo.getSergeantDate(),
			militaryInfo.getDischargeDate()
		);
	}
}

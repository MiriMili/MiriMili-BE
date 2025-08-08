package org.example.mirimilibe.member.dto;

import java.time.LocalDate;

import org.example.mirimilibe.common.Enum.MiliType;


public record MilitaryInfoReq(
	MiliType type,
	Long specialtyId,
	Long unitId,
	LocalDate startDate,
	LocalDate privateDate,
	LocalDate corporalDate,
	LocalDate sergeantDate,
	LocalDate dischargeDate
) {

}

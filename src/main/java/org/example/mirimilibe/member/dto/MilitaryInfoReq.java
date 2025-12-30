package org.example.mirimilibe.member.dto;

import java.time.LocalDate;

import org.example.mirimilibe.common.Enum.MiliType;

import jakarta.validation.constraints.NotNull;

public record MilitaryInfoReq(
	@NotNull
	MiliType type,
	String specialty,
	String unit,
	LocalDate startDate,
	LocalDate privateDate,
	LocalDate corporalDate,
	LocalDate sergeantDate,
	LocalDate dischargeDate
) {

}

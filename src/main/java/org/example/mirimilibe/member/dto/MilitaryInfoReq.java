package org.example.mirimilibe.member.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;

import com.fasterxml.jackson.annotation.JsonFormat;

public record MilitaryInfoReq(
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

}

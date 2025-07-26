package org.example.mirimilibe.member.dto;

import java.time.LocalDateTime;
import java.util.Date;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;

public record MilitaryInfoReq(
	MiliStatus status,
	MiliType type,
	Long specialtyId,
	Long unitId,
	Date startDate,
	Date privateDate,
	Date corporalDate,
	Date sergeantDate,
	Date dischargeDate
) {

}

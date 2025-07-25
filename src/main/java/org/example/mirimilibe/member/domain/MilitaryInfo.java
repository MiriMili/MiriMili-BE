package org.example.mirimilibe.member.domain;

import java.util.Date;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "military_info")
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryInfo {
	@Id
	private Long id;

	@OneToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@Enumerated(EnumType.STRING)
	private MiliStatus miliStatus;

	@Enumerated(EnumType.STRING)
	private MiliType miliType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specialty")
	private Specialty specialty;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unit")
	private Unit unit;

	private Date startDate;
	private Date privateDate;
	private Date corporalDate;
	private Date sergeantDate;
	private Date dischargeDate;
}
package org.example.mirimilibe.member.domain;

import java.util.Date;

import org.example.mirimilibe.common.Enum.MiliType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "military_info")
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryInfo {
	@Id
	private Long id;

	@OneToOne
	@JoinColumn(name = "id")
	private Member member;

	@Enumerated(EnumType.STRING)
	private MiliType miliType;

	private Long specialty;
	private Long unit;

	private Date startDate;
	private Date privateDate;
	private Date corporalDate;
	private Date sergeantDate;
	private Date dischargeDate;
}
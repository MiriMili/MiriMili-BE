package org.example.mirimilibe.member.domain;

import java.time.LocalDate;

import org.example.mirimilibe.common.Enum.MiliRank;
import org.example.mirimilibe.common.Enum.MiliStatus;

public class MiliRankCalculator {

	public static MiliRank getCurrentRank(MilitaryInfo militaryInfo,LocalDate today) {

		if(militaryInfo == null || militaryInfo.getMiliStatus()== MiliStatus.PRE_ENLISTED) {
			return MiliRank.BEFORE_ENLISTMENT;
		}

		if(militaryInfo.getMiliStatus()== MiliStatus.DISCHARGED) {
			return MiliRank.DISCHARGED;
		}

		return calculateRank(militaryInfo, today);
	}

	private static MiliRank calculateRank(MilitaryInfo info, LocalDate today) {
		// null → 아직 진급 안 한 상태로 해석
		if (info.getPrivateDate() == null || today.isBefore(info.getPrivateDate())) {
			return MiliRank.PRIVATE;
		}
		if (info.getCorporalDate() == null || today.isBefore(info.getCorporalDate())) {
			return MiliRank.PRIVATE_FIRST;
		}
		if (info.getSergeantDate() == null || today.isBefore(info.getSergeantDate())) {
			return MiliRank.CORPORAL;
		}
		if (info.getDischargeDate() == null || today.isBefore(info.getDischargeDate())) {
			return MiliRank.SERGEANT;
		}

		return MiliRank.DISCHARGED;
	}
}

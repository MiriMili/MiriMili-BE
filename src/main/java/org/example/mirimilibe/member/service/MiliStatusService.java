package org.example.mirimilibe.member.service;

import java.time.LocalDate;

import org.example.mirimilibe.common.Enum.MiliStatus;
import org.example.mirimilibe.global.error.MilitaryInfoErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MiliStatusService {

	@Transactional
	public void updateMiliStatusToEnlisted(MilitaryInfo militaryInfo) {
		if(militaryInfo.getMiliStatus() != MiliStatus.PRE_ENLISTED) {
			throw new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_CANNOT_UPDATE);
		}

		militaryInfo.setMiliStatus(MiliStatus.ENLISTED);
	}

	@Transactional
	public void updateMiliStatusToDischarged(MilitaryInfo militaryInfo) {
		if(militaryInfo.getMiliStatus() == MiliStatus.ENLISTED && militaryInfo.getDischargeDate()!= null
			&& !LocalDate.now().isBefore(militaryInfo.getDischargeDate())){
			militaryInfo.setMiliStatus(MiliStatus.DISCHARGED);
		}
	}
}

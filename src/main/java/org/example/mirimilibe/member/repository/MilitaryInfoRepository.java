package org.example.mirimilibe.member.repository;

import java.util.Optional;

import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilitaryInfoRepository extends JpaRepository<MilitaryInfo,Long> {
	Optional<MilitaryInfo> findByMemberId(Long memberId);

}

package org.example.mirimilibe.member.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MilitaryInfoRepository extends JpaRepository<MilitaryInfo,Long> {
	Optional<MilitaryInfo> findByMemberId(Long memberId);

	boolean existsByMemberId(Long memberId);

	@Query("""
		SELECT mi FROM MilitaryInfo mi
		LEFT JOIN FETCH mi.specialty
		LEFT JOIN FETCH mi.unit
		WHERE mi.member.id = :memberId
	""")
	Optional<MilitaryInfo> findByMemberIdWithSpecialty(@Param("memberId") Long memberId);

	@Query("""
		SELECT mi FROM MilitaryInfo mi
		LEFT JOIN FETCH mi.specialty
		LEFT JOIN FETCH mi.unit
		WHERE mi.member.id IN :memberIds
	""")
	List<MilitaryInfo> findAllByMemberIdsWithSpecialty(@Param("memberIds") List<Long> memberIds);

	default Map<Long, MilitaryInfo> findMapByMemberIds(List<Long> memberIds) {
		return findAllByMemberIdsWithSpecialty(memberIds).stream()
			.collect(Collectors.toMap(mi -> mi.getMember().getId(), mi -> mi));
	}

}

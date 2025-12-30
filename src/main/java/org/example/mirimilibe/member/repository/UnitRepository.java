package org.example.mirimilibe.member.repository;

import java.util.Optional;

import org.example.mirimilibe.common.domain.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
	Optional<Unit> findByValue(String value);
}

package org.example.mirimilibe.post.repository;

import java.util.Optional;

import org.example.mirimilibe.common.domain.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

	Optional<Specialty> findByValue(String value);
}

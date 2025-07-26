package org.example.mirimilibe.member.repository;

import org.example.mirimilibe.member.domain.MemberTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermRepository extends JpaRepository<MemberTerm, Long> {
}

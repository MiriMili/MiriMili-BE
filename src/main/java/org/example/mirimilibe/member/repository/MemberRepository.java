package org.example.mirimilibe.member.repository;

import org.example.mirimilibe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

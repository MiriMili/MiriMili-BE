package org.example.mirimilibe.member.repository;

import java.util.Optional;

import org.example.mirimilibe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByNumber(String phoneNumber);

}

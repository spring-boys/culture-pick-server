package com.ssafy.culturepick.member.repository;

import com.ssafy.culturepick.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderId(String providerId);
}

package com.fourseason.delivery.domain.member.repository;

import com.fourseason.delivery.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsernameAndDeletedAtIsNull(String username);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String admin);
}

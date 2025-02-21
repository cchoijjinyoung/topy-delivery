package com.fourseason.delivery.domain.member.repository;

import com.fourseason.delivery.domain.member.entity.Address;
import com.fourseason.delivery.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByMemberAndDeletedAtIsNull(Member member);

    Optional<Address> findByIdAndDeletedAtIsNull(UUID addressId);
}

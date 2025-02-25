package com.fourseason.delivery.domain.shop.repository;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {

    Optional<Shop> findByIdAndDeletedAtIsNull(UUID id);

    List<Shop> findByMemberAndDeletedAtIsNull(Member member);
}

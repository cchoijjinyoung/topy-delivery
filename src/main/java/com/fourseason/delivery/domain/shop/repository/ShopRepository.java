package com.fourseason.delivery.domain.shop.repository;

import com.fourseason.delivery.domain.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {

    Optional<Shop> findByIdAndDeletedAtIsNull(UUID id);
}

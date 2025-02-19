package com.fourseason.delivery.domain.shop.repository;

import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.entity.ShopImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShopImageRepository extends JpaRepository<ShopImage, UUID> {

    List<ShopImage> findByShopId(UUID shopId);
}

package com.fourseason.delivery.domain.menu.repository;

import com.fourseason.delivery.domain.menu.entity.Menu;

import com.fourseason.delivery.domain.menu.entity.MenuStatus;
import com.fourseason.delivery.domain.shop.entity.Shop;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface MenuRepository extends JpaRepository<Menu, UUID> {
    Optional<Menu> findByIdAndDeletedAtIsNull(UUID id);
    List<Menu> findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
        List<UUID> menuIds, MenuStatus menuStatus, Shop shop);
}

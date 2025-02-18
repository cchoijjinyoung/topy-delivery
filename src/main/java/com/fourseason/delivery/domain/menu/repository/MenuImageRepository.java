package com.fourseason.delivery.domain.menu.repository;

import com.fourseason.delivery.domain.menu.entity.MenuImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuImageRepository extends JpaRepository<MenuImage, UUID> {
    List<MenuImage> findByMenuId(UUID id);
}

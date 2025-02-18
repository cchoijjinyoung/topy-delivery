package com.fourseason.delivery.domain.menu.repository;

import com.fourseason.delivery.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    Optional<Menu> findByIdAndDeletedAtIsNull(UUID id);
}

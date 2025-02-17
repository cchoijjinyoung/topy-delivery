package com.fourseason.delivery.domain.menu.repository;

import com.fourseason.delivery.domain.menu.entity.Menu;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

  // TODO: 충돌 해결
  List<Menu> findByIdIn(List<UUID> menuIds);
}

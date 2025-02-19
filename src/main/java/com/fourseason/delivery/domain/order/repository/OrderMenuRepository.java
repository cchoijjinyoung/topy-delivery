package com.fourseason.delivery.domain.order.repository;

import com.fourseason.delivery.domain.order.entity.OrderMenu;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, UUID> {

}

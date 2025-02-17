package com.fourseason.delivery.domain.order.repository;

import com.fourseason.delivery.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}

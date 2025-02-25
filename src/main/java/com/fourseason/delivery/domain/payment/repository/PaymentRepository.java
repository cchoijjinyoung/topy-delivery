package com.fourseason.delivery.domain.payment.repository;

import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.payment.entity.Payment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  Optional<Payment> findByIdAndDeletedAtIsNull(final UUID id);

  Optional<Payment> findByOrderAndDeletedAtIsNull(Order order);
}

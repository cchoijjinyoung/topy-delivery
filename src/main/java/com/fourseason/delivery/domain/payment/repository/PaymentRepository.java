package com.fourseason.delivery.domain.payment.repository;

import com.fourseason.delivery.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdAndDeletedAtIsNotNull(UUID id);
}

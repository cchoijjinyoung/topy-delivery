package com.fourseason.delivery.domain.ai.repository;

import com.fourseason.delivery.domain.ai.entity.AiResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiResponseRepository extends JpaRepository<AiResponse, UUID> {
}

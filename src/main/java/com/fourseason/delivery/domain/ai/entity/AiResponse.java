package com.fourseason.delivery.domain.ai.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_ai_response")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiResponse extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private String response;

    @Builder
    private AiResponse(String response) {
        this.response = response;
    }

    public static AiResponse addOf(String response) {
        return AiResponse.builder()
            .response(response)
            .build();
    }
}

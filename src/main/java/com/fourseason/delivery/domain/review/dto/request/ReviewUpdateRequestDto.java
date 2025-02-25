package com.fourseason.delivery.domain.review.dto.request;

import java.util.List;
import java.util.UUID;

public record ReviewUpdateRequestDto (
        String content,
        int rating,
        List<UUID> unchangedImageId) {
}

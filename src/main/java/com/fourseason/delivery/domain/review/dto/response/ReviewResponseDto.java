package com.fourseason.delivery.domain.review.dto.response;

import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record ReviewResponseDto (
        String content,
        int rating,
        String username,
        String shopname,
        LocalDateTime createdAt,
        List<UUID> imageIds,
        List<String> images
) {
    public static ReviewResponseDto of(Review review, List<ReviewImage> reviewImages) {
        return ReviewResponseDto.builder()
                .content(review.getContent())
                .rating(review.getRating())
                .username(review.getMember().getUsername())
                .shopname(review.getShop().getName())
                .createdAt(review.getCreatedAt())
                .imageIds(reviewImages.stream()
                        .map(ReviewImage::getId)
                        .toList())
                .images(reviewImages.stream()
                        .map(ReviewImage::getImageUrl)
                        .toList())
                .build();
    }
}

package com.fourseason.delivery.domain.review.dto.response;

import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record ReviewResponseDto (
        String content,
        int rating,
        String username,
        String shopname,
        LocalDateTime createdAt,
        List<String> imageUrls
) {
    public static ReviewResponseDto of(Review review, List<ReviewImage> reviewImages) {
        return ReviewResponseDto.builder()
                .content(review.getContent())
                .rating(review.getRating())
                .username(review.getMember().getUsername())
                .shopname(review.getShop().getName())
                .createdAt(review.getCreatedAt())
                .imageUrls(reviewImages.stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }
}

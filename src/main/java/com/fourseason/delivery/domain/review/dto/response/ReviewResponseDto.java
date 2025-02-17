package com.fourseason.delivery.domain.review.dto.response;

import com.fourseason.delivery.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponseDto {
    private String content;
    private int rating;
    private String username;

    public static ReviewResponseDto of(Review review) {
        return ReviewResponseDto.builder()
                .content(review.getContent())
                .rating(review.getRating())
                .username(review.getMember().getUsername())
                .build();
    }
}

package com.fourseason.delivery.domain.review.controller;

import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.repository.ReviewRepository;
import com.fourseason.delivery.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShopReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    /**
     * 특정 가게 리뷰 리스트 조회
     */
    @GetMapping("/api/shops/{shop_id}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviewList(@PathVariable UUID shopId) {
        return ResponseEntity.ok(reviewService.getReviewList(shopId));
    }
}

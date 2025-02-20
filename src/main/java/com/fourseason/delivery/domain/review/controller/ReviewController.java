package com.fourseason.delivery.domain.review.controller;

import com.fourseason.delivery.domain.review.dto.request.ReviewRequestDto;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{order_id}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 등록
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createReview(@PathVariable UUID orderId,
                                             @RequestPart("review") ReviewRequestDto reviewRequestDto) {
        reviewService.createReview(orderId, reviewRequestDto);
        return ResponseEntity.ok().build();
    }


    /**
     * 특정 리뷰 조회
     */
    @GetMapping("/{review_id}")
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable UUID orderId,
                                                       @PathVariable UUID reviewId) {
        return ResponseEntity.ok(reviewService.getReview(orderId, reviewId));
    }

    /**
     * 리뷰 수정
     */
    @PostMapping("/{review_id}")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable UUID orderId,
                                                          @PathVariable UUID reviewId,
                                                          @RequestBody ReviewRequestDto reviewRequestDto) {
        return ResponseEntity.ok(reviewService.updateReview(orderId, reviewId, reviewRequestDto));
    }


    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{review_id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID orderId,
                                             @PathVariable UUID reviewId) {
        reviewService.deleteReview(orderId, reviewId);
        return ResponseEntity.ok().build();
    }

}

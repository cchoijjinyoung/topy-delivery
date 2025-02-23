package com.fourseason.delivery.domain.review.controller;

import com.fourseason.delivery.domain.review.dto.request.ReviewRequestDto;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{order_id}/reviews")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 등록
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createReview(@PathVariable("order_id") UUID orderId,
                                             @Valid @RequestPart("review") ReviewRequestDto reviewRequestDto,
                                             @RequestPart(required = false) List<MultipartFile> images) {
        reviewService.createReview(orderId, reviewRequestDto, images);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 리뷰 조회
     */
    @GetMapping("/{review_id}")
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable("order_id") UUID orderId,
                                                       @PathVariable("review_id") UUID reviewId) {
        return ResponseEntity.ok(reviewService.getReview(orderId, reviewId));
    }

    /**
     * 리뷰 수정
     */
    @PutMapping(value = "/{review_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable("order_id") UUID orderId,
                                                          @PathVariable("review_id") UUID reviewId,
                                                          @Valid @RequestPart("review") ReviewRequestDto reviewRequestDto,
                                                          @RequestPart(required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(reviewService.updateReview(orderId, reviewId, reviewRequestDto, images));
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{review_id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("order_id") UUID orderId,
                                             @PathVariable("review_id") UUID reviewId) {
        reviewService.deleteReview(orderId, reviewId);
        return ResponseEntity.ok().build();
    }

}

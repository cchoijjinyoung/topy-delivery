package com.fourseason.delivery.domain.review.controller;

import com.fourseason.delivery.domain.review.dto.request.ReviewRequestDto;
import com.fourseason.delivery.domain.review.dto.request.ReviewUpdateRequestDto;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.service.ReviewService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
                                                          @Valid @RequestPart("review") ReviewUpdateRequestDto reviewUpdateRequestDto,
                                                          @RequestPart(required = false) List<MultipartFile> newImages,
                                                          @AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(reviewService.updateReview(principal.getId(), orderId, reviewId, reviewUpdateRequestDto, newImages));
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{review_id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("order_id") UUID orderId,
                                             @PathVariable("review_id") UUID reviewId,
                                             @AuthenticationPrincipal CustomPrincipal principal) {
        reviewService.deleteReview(principal.getId(), orderId, reviewId);
        return ResponseEntity.ok().build();
    }

}

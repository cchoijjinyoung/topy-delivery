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

@RestController
@RequestMapping("/api/orders/{order_id}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 등록
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createReview(@PathVariable String order_id,
                                             @RequestPart("review") ReviewRequestDto reviewRequestDto,
                                             @RequestPart(value = "images", required = false)List<MultipartFile> images) {
        reviewService.createReview(order_id, reviewRequestDto, images);
        return ResponseEntity.ok().build();
    }


    /**
     * 특정 리뷰 조회
     */
    @GetMapping("/{review_id}")
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable String order_id,
                                                       @PathVariable String review_id) {
        return ResponseEntity.ok(reviewService.getReview(order_id, review_id));
    }

    /**
     * 리뷰 수정
     */
    @PostMapping("/{review_id}")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable String order_id,
                                                          @PathVariable String review_id,
                                                          @RequestBody ReviewRequestDto reviewRequestDto) {
        return ResponseEntity.ok(reviewService.updateReview(order_id, review_id, reviewRequestDto));
    }


    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{review_id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String order_id,
                                             @PathVariable String review_id) {
        reviewService.deleteReview(order_id, review_id);
        return ResponseEntity.ok().build();
    }

}

package com.fourseason.delivery.domain.review.service;

import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.review.dto.request.ReviewRequestDto;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import com.fourseason.delivery.domain.review.exception.ReviewErrorCode;
import com.fourseason.delivery.domain.review.repository.ReviewImageRepository;
import com.fourseason.delivery.domain.review.repository.ReviewRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final ReviewImageService imageUploadService;

    @Transactional
    public void createReview(String order_id, ReviewRequestDto reviewRequestDto, List<MultipartFile> images) {
        // 1) Order 조회
        Order order = orderRepository.findById(order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.ORDER_NOT_FOUND));

        // 2) Review 저장
        Review review = Review.addOf(reviewRequestDto, order);
        reviewRepository.save(review);

        // 3) 이미지 업로드, ReviewImage 저장
        if(images != null && !images.isEmpty()) {
            for(MultipartFile image : images) {
                String imageUrl = imageUploadService.uploadImage(image);

                ReviewImage reviewImage = ReviewImage.of(review, imageUrl);
                reviewImageRepository.save(reviewImage);
            }
        }
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(String order_id, String review_id) {
        Review review = reviewRepository.findByIdAndOrderId(UUID.fromString(review_id), order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        return ReviewResponseDto.of(review);
    }

    @Transactional
    public ReviewResponseDto updateReview(String order_id, String review_id, ReviewRequestDto reviewRequestDto) {
        Review review = reviewRepository.findByIdAndOrderId(UUID.fromString(review_id), order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        review.updateOf(reviewRequestDto);

        return ReviewResponseDto.of(review);
    }

    @Transactional
    public void deleteReview(String order_id, String review_id) {
        Review review = reviewRepository.findByIdAndOrderId(UUID.fromString(review_id), order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
    }


    public List<ReviewResponseDto> getReviewList(String shop_id) {
        Shop shop = shopRepository.findById(UUID.fromString(shop_id))
                .orElseThrow(() -> new CustomException(ReviewErrorCode.SHOP_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByShop(shop);

        return reviews.stream()
                .map(ReviewResponseDto::of)
                .collect(Collectors.toList());
    }
}

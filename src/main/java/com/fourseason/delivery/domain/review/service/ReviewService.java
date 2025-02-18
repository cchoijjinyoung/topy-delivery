package com.fourseason.delivery.domain.review.service;

import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
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

import java.util.ArrayList;
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
    public void createReview(String order_id, ReviewRequestDto reviewRequestDto) {
        // 1) Order 조회
        Order order = orderRepository.findById(order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.ORDER_NOT_FOUND));

        // 2) 주문상태 확인
        if(order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_COMPLETED);
        }

        // 3) Review 저장
        Review review = Review.addOf(reviewRequestDto, order);
        reviewRepository.save(review);

        // 4) 이미지 업로드, ReviewImage 저장
        if(reviewRequestDto.images() != null && !reviewRequestDto.images().isEmpty()) {
            for(MultipartFile image : reviewRequestDto.images()) {
                String imageUrl = imageUploadService.uploadImage(image);
                ReviewImage reviewImage = ReviewImage.of(review, imageUrl);
                reviewImageRepository.save(reviewImage);
            }
        }
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(String order_id, String review_id) {
        // 1) 리뷰 조회
        Review review = reviewRepository.findByIdAndOrderId(UUID.fromString(review_id), order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 2) 리뷰 이미지 리스트 조회
        List<ReviewImage> reviewImages = reviewImageRepository.findByReviewId(UUID.fromString(review_id));

        return ReviewResponseDto.of(review, reviewImages);
    }

    @Transactional
    public ReviewResponseDto updateReview(String order_id, String review_id, ReviewRequestDto reviewRequestDto) {
        // 1) 리뷰 조회 및 리뷰 내용, 평점 업데이트
        Review review = reviewRepository.findByIdAndOrderId(UUID.fromString(review_id), order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        review.updateOf(reviewRequestDto);

        // 2) 새로운 이미지가 있을 경우 기존 이미지 삭제
        List<ReviewImage> existingImages = reviewImageRepository.findByReviewId(UUID.fromString(review_id));
        if(!existingImages.isEmpty()) {
            for(ReviewImage image : existingImages) {
                reviewImageRepository.delete(image);
            }
        }

        // 3) 새로운 이미지 추가
        List<ReviewImage> images = new ArrayList<>();
        if(reviewRequestDto.images() != null && !reviewRequestDto.images().isEmpty()) {
            for(MultipartFile image : reviewRequestDto.images()) {
                String imageUrl = imageUploadService.uploadImage(image);
                ReviewImage reviewImage = ReviewImage.of(review, imageUrl);
                images.add(reviewImage);
                reviewImageRepository.save(reviewImage);
            }
        }

        return ReviewResponseDto.of(review, images);
    }

    @Transactional
    public void deleteReview(String order_id, String review_id) {
        Review review = reviewRepository.findByIdAndOrderId(UUID.fromString(review_id), order_id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        List<ReviewImage> existingImages = reviewImageRepository.findByReviewId(UUID.fromString(review_id));
        if(!existingImages.isEmpty()) {
            for(ReviewImage image : existingImages) {
                reviewImageRepository.delete(image);
            }
        }

        reviewRepository.delete(review);
    }


    public List<ReviewResponseDto> getReviewList(String shop_id) {
        Shop shop = shopRepository.findById(UUID.fromString(shop_id))
                .orElseThrow(() -> new CustomException(ReviewErrorCode.SHOP_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByShop(shop);
        List<ReviewImage> images = reviewImageRepository.findByReviewId(UUID.fromString(shop_id));

        // 각 리뷰마다 해당하는 이미지 찾아서 ReviewResponseDto에 넣기
        List<ReviewResponseDto> responseList = new ArrayList<>();
        for(Review review : reviews) {
            List<ReviewImage> imagesForReview = new ArrayList<>();
            for(ReviewImage image : images) {
                if(review.equals(image.getReview())) {
                    imagesForReview.add(image);
                }
            }

            responseList.add(ReviewResponseDto.of(review, imagesForReview));
        }

        return responseList;
    }
}

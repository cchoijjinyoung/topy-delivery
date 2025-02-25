package com.fourseason.delivery.domain.review.service;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.service.FileService;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.service.MemberQueryService;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.review.dto.request.ReviewRequestDto;
import com.fourseason.delivery.domain.review.dto.request.ReviewUpdateRequestDto;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final FileService fileService;
    private final MemberQueryService memberQueryService;


    @Transactional
    public void createReview(UUID orderId, ReviewRequestDto reviewRequestDto, List<MultipartFile> images) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.ORDER_NOT_FOUND));

        if (order.getCreatedAt().isBefore(LocalDateTime.now().minusDays(5))) {
            throw new CustomException(ReviewErrorCode.REVIEW_PERIOD_EXPIRED);
        }

        if(order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_COMPLETED);
        }

        if(reviewRepository.findByOrderIdAndDeletedAtIsNull(orderId) != null) {
            throw new CustomException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.addOf(reviewRequestDto, order);
        reviewRepository.save(review);

        if(images != null && !images.isEmpty()) {
            for(MultipartFile image : images) {
                fileService.saveImageFile(S3Folder.REVIEW, image, review.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(UUID orderId, UUID reviewId) {
        // 1) 리뷰 조회
        Review review = reviewRepository.findByIdAndOrderIdAndDeletedAtIsNull(reviewId, orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 2) 리뷰 이미지 리스트 조회
        List<ReviewImage> reviewImages = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(reviewId);

        return ReviewResponseDto.of(review, reviewImages);
    }

    @Transactional
    public ReviewResponseDto updateReview(Long memberId, UUID orderId, UUID reviewId, ReviewUpdateRequestDto reviewUpdateRequestDto, List<MultipartFile> newImages) {
        // 1) 리뷰 조회 및 리뷰 내용, 평점 업데이트
        Review review = reviewRepository.findByIdAndOrderIdAndDeletedAtIsNull(reviewId, orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        review.updateOf(reviewUpdateRequestDto);

        Member member = memberQueryService.findActiveMember(memberId);

        List<UUID> existingReviewImageIdList = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(reviewId).stream()
                .map(ReviewImage::getId)
                .toList();

        if(existingReviewImageIdList != null && !existingReviewImageIdList.isEmpty()) {
            for(UUID id : existingReviewImageIdList) {
                if(!reviewUpdateRequestDto.unchangedImageId().contains(id)) {
                    ReviewImage reviewImage = reviewImageRepository.findByIdAndDeletedAtIsNull(id)
                            .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_IMAGE_NOT_FOUND));
                    reviewImage.deleteOf(member.getUsername());
                }
            }
        }

        if(newImages != null && !newImages.isEmpty()) {
            for(MultipartFile image : newImages) {
                fileService.saveImageFile(S3Folder.REVIEW, image, review.getId());
            }
        }

        List<ReviewImage> images = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(reviewId);

        return ReviewResponseDto.of(review, images);
    }

    @Transactional
    public void deleteReview(Long memberId, UUID orderId, UUID reviewId) {
        Review review = reviewRepository.findByIdAndOrderIdAndDeletedAtIsNull(reviewId, orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        Member member = memberQueryService.findActiveMember(memberId);

        List<ReviewImage> existingImages = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(reviewId);
        if(!existingImages.isEmpty()) {
            for(ReviewImage image : existingImages) {
                image.deleteOf(member.getUsername());
            }
        }

        review.deleteOf(member.getUsername());
    }


    public List<ReviewResponseDto> getReviewList(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.SHOP_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByShopAndDeletedAtIsNull(shop);
        List<ReviewImage> images = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(shopId);

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

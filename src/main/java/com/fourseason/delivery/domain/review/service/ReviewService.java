package com.fourseason.delivery.domain.review.service;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.service.FileService;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
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

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final FileService fileService;


    @Transactional
    public void createReview(UUID orderId, ReviewRequestDto reviewRequestDto, List<MultipartFile> images) {
        // 1) Order 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.ORDER_NOT_FOUND));

        // 2) 주문상태 확인
        if(order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_COMPLETED);
        }

        // 3) 기존 리뷰가 있는지 확인
        if(reviewRepository.findByOrderIdAndDeletedAtIsNull(orderId) != null) {
            throw new CustomException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 4) Review 저장
        Review review = Review.addOf(reviewRequestDto, order);
        reviewRepository.save(review);

        // 4) 이미지 업로드, ReviewImage 저장
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
    public ReviewResponseDto updateReview(UUID orderId, UUID reviewId, ReviewRequestDto reviewRequestDto, List<MultipartFile> images) {
        // 1) 리뷰 조회 및 리뷰 내용, 평점 업데이트
        Review review = reviewRepository.findByIdAndOrderIdAndDeletedAtIsNull(reviewId, orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        review.updateOf(reviewRequestDto);

        // TODO: 현재 임시 유저를 넣었음. 이후 수정 필요.
        Member member = new Member("유저", "user@example.com", "1234", "010-0000-0000", Role.CUSTOMER);


        // 2) 새로운 이미지가 있을 경우 기존 이미지 삭제
        List<ReviewImage> existingImages = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(reviewId);
        if(!existingImages.isEmpty()) {
            for(ReviewImage image : existingImages) {
                image.deleteOf(member.getUsername());
            }
        }

        // 3) 새로운 이미지 추가
        if(images != null && !images.isEmpty()) {
            for(MultipartFile image : images) {
                fileService.saveImageFile(S3Folder.REVIEW, image, review.getId());
            }
        }
        List<ReviewImage> newImages = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(reviewId);

        return ReviewResponseDto.of(review, newImages);
    }

    @Transactional
    public void deleteReview(UUID orderId, UUID reviewId) {
        Review review = reviewRepository.findByIdAndOrderIdAndDeletedAtIsNull(reviewId, orderId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // TODO: 현재 임시 유저를 넣었음. 이후 수정 필요.
        Member member = new Member("유저", "user@example.com", "1234", "010-0000-0000", Role.CUSTOMER);

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

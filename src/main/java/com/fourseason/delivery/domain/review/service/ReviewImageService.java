package com.fourseason.delivery.domain.review.service;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.service.S3Service;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import com.fourseason.delivery.domain.review.exception.ReviewErrorCode;
import com.fourseason.delivery.domain.review.exception.ReviewImageErrorCode;
import com.fourseason.delivery.domain.review.repository.ReviewImageRepository;
import com.fourseason.delivery.domain.review.repository.ReviewRepository;
import com.fourseason.delivery.global.exception.CustomException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final S3Service s3Service;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewRepository reviewRepository;

    @Value("${file.image-extension}")
    private String imageExtension;

    @Transactional
    public void saveImageFile(S3Folder s3Folder, MultipartFile file, UUID id) {
        // S3 폴더 파라미터 확인
        if (s3Folder == null) {
            throw new CustomException(ReviewImageErrorCode.IMAGE_UPLOAD_FAILED);
        }

        // 파일이 비어있는지 확인
        if(file == null || file.isEmpty()) {
            throw new CustomException(ReviewImageErrorCode.IMAGE_UPLOAD_FAILED);
        }

        // 파일 확장자 확인
        final String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(StringUtils.isBlank(fileExtension) || !imageExtension.contains(fileExtension)) {
            throw new CustomException(ReviewImageErrorCode.INVALID_FILE_EXTENSION);
        }

        // 파일 이름 생성
        final String fileName = UUID.randomUUID() + "." + fileExtension;

        // S3에 파일 업로드
        s3Service.uploadFile(fileName, s3Folder, file);

        // 파일 정보 저장
        if(s3Folder == S3Folder.REVIEW) {
            saveReviewImageInfo(s3Folder, file, id, fileName);
        }
    }


    private void saveReviewImageInfo(final S3Folder s3Folder,
                                     final MultipartFile file,
                                     final UUID id,
                                     final String fileName) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        ReviewImage reviewImage = ReviewImage.builder()
                .originalFileName(file.getOriginalFilename())
                .imageUrl(fileName)
                .fileSize(file.getSize())
                .s3Folder(s3Folder.getFolderName())
                .review(review)
                .build();
        reviewImageRepository.save(reviewImage);
    }

}

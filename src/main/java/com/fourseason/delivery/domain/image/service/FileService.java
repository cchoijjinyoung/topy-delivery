package com.fourseason.delivery.domain.image.service;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.exception.ImageErrorCode;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.entity.MenuImage;
import com.fourseason.delivery.domain.menu.exception.MenuErrorCode;
import com.fourseason.delivery.domain.menu.repository.MenuImageRepository;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import com.fourseason.delivery.domain.review.exception.ReviewErrorCode;
import com.fourseason.delivery.domain.review.repository.ReviewImageRepository;
import com.fourseason.delivery.domain.review.repository.ReviewRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.entity.ShopImage;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.domain.shop.repository.ShopImageRepository;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.exception.CustomException;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.io.FilenameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;

    private final ShopImageRepository shopImageRepository;

    private final MenuImageRepository menuImageRepository;

    private final ShopRepository shopRepository;

    private final MenuRepository menuRepository;

    private final ReviewRepository reviewRepository;

    private final ReviewImageRepository reviewImageRepository;

    @Value("${file.image-extension}")
    private String imageExtension;

    @Transactional
    public void saveImageFile(S3Folder s3Folder, MultipartFile file, UUID id) {
        // S3 폴더 파라미터 확인
        if (s3Folder == null) {
            throw new CustomException(ImageErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new CustomException(ImageErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        // 파일 확장자 확인
        final String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isBlank(fileExtension) || !imageExtension.contains(fileExtension)) {
            throw new CustomException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }

        // 파일 이름 생성
        final String fileName = UUID.randomUUID() + "." + fileExtension;

        // S3에 파일 업로드
        s3Service.uploadFile(fileName, s3Folder, file);

        // 파일 정보 저장
        if (s3Folder == S3Folder.SHOP) {
            saveShopImageInfo(s3Folder, file, id, fileName);
        } else if (s3Folder == S3Folder.MENU) {
            saveMenuImageInfo(s3Folder, file, id, fileName);
        }
    }

//    @Transactional
//    public void removeFile(final UUID id) {
//        // 파일 정보 조회
//        AtchFile atchFile = atchFileRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ImageErrorCode.ATCH_FILE_NOT_FOUND));
//
//        // S3에서 파일 삭제
//        s3Service.removeFile(atchFile.getStoredFileName(), S3Folder.valueOf(atchFile.getS3Folder().toUpperCase()));
//
//        // 파일 정보 삭제
//        atchFileRepository.delete(atchFile);
//    }

    private void saveShopImageInfo(final S3Folder s3Folder,
                                   final MultipartFile file,
                                   final UUID id,
                                   final String fileName) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        ShopImage shopImage = ShopImage.builder()
            .originalFileName(file.getOriginalFilename())
            .imageUrl(fileName)
            .fileSize(file.getSize())
            .s3Folder(s3Folder.getFolderName())
            .shop(shop)
            .build();
        shopImageRepository.save(shopImage);
    }

    private void saveMenuImageInfo(final S3Folder s3Folder,
                                   final MultipartFile file,
                                   final UUID id,
                                   final String fileName) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));
        MenuImage menuImage = MenuImage.builder()
            .originalFileName(file.getOriginalFilename())
            .imageUrl(fileName)
            .fileSize(file.getSize())
            .s3Folder(s3Folder.getFolderName())
            .menu(menu)
            .build();
        menuImageRepository.save(menuImage);
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

    /**
     * 파일이 존재하는지 확인
     *
     * @param file 파일
     * @return 파일이 존재하는지 여부
     */
    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * 파일이 존재하지 않는지 확인
     *
     * @param file 파일
     * @return 파일이 존재하지 않는지 여부
     */
    private static boolean hasNotFile(MultipartFile file) {
        return !hasFile(file);
    }
}

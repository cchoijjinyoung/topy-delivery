package com.fourseason.delivery.domain.review.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourseason.delivery.domain.review.exception.ReviewErrorCode;
import com.fourseason.delivery.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ReviewImageService {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public ReviewImageService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile file) {
        try {
            // 파일명
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // S3에 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 업로드된 파일의 URL 반환
            return s3Client.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new CustomException(ReviewErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}

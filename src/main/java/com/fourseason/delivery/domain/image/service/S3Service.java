package com.fourseason.delivery.domain.image.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.exception.ImageErrorCode;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    public void uploadFile(final String fileName, S3Folder s3Folder, MultipartFile multipartFile) {
        // S3 폴더 파라미터 확인
        if (s3Folder == null) {
            throw new CustomException(ImageErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        // 파일이 비어있는지 확인
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException(ImageErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType(multipartFile.getContentType());
        metaData.setContentLength(multipartFile.getSize());

        final String bucketName = bucket + "/" + s3Folder.getFolderName();

        try {
            amazonS3Client.putObject(bucketName, fileName, multipartFile.getInputStream(), metaData);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new CustomException(ImageErrorCode.FAILED_TO_UPLOAD_FILE);
        }
    }

    public void removeFile(final String fileName, S3Folder s3Folder) {
        try {
            amazonS3Client.deleteObject(bucket + "/" + s3Folder.getFolderName(), fileName);
        } catch (SdkClientException e) {
            throw new CustomException(ImageErrorCode.FAILED_TO_DELETE_FILE);
        }
    }
}

package com.fourseason.delivery.domain.review.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_review_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    @Comment("원 파일명")
    private String originalFileName;

    @Column(nullable = false, updatable = false)
    private long fileSize;

    @Column(nullable = false, updatable = false)
    private String s3Folder;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @Builder
    public ReviewImage(String imageUrl, String originalFileName, long fileSize, String s3Folder, Review review) {
        this.imageUrl = imageUrl;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.s3Folder = s3Folder;
        this.review = review;
    }

    public static ReviewImage of(Review review, String imageUrl) {
        ReviewImage reviewImage = new ReviewImage();
        reviewImage.review = review;
        reviewImage.imageUrl = imageUrl;
        return reviewImage;
    }

    public void deleteOf(String deletedBy) {
        super.deleteOf(deletedBy);
    }
}
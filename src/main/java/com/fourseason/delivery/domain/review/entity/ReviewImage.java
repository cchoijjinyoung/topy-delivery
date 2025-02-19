package com.fourseason.delivery.domain.review.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    public static ReviewImage of(Review review, String imageUrl) {
        ReviewImage reviewImage = new ReviewImage();
        reviewImage.review = review;
        reviewImage.imageUrl = imageUrl;
        return reviewImage;
    }

}
package com.fourseason.delivery.domain.review.entity;

import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_review_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;
}

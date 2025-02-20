package com.fourseason.delivery.domain.shop.entity;

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
@Table(name = "p_shop_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopImage extends BaseTimeEntity {

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
    @Comment("S3 폴더명")
    private String s3Folder;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Builder
    public ShopImage(String imageUrl, String originalFileName, long fileSize, String s3Folder, Shop shop) {
        this.imageUrl = imageUrl;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.s3Folder = s3Folder;
        this.shop = shop;
    }
}

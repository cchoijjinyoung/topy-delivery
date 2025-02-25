package com.fourseason.delivery.domain.menu.entity;

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
@Table(name = "p_menu_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuImage extends BaseTimeEntity {

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
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Builder
    private MenuImage(String imageUrl, String originalFileName, long fileSize, String s3Folder, Menu menu) {
        this.imageUrl = imageUrl;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.s3Folder = s3Folder;
        this.menu = menu;
    }
}

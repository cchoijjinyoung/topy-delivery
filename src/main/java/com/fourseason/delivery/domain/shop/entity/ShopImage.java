package com.fourseason.delivery.domain.shop.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_shop_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopImage extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}

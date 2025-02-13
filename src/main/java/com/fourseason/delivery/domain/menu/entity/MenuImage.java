package com.fourseason.delivery.domain.menu.entity;

import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_menu_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuImage extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
}

package com.fourseason.delivery.domain.menu.entity;

import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus menuStatus;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Builder
    public Menu(String name, String description, int price, MenuStatus menuStatus, Shop shop) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.menuStatus = menuStatus;
        this.shop = shop;
    }

    @PrePersist
    public void prePersist() {
        this.menuStatus = this.menuStatus == null ? MenuStatus.SHOW : this.menuStatus;
    }
}

package com.fourseason.delivery.domain.menu.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.menu.dto.request.CreateMenuRequestDto;
import com.fourseason.delivery.domain.menu.dto.request.UpdateMenuRequestDto;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

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

    public static Menu addOf(CreateMenuRequestDto dto, Shop shop) {
        return Menu.builder()
            .name(dto.name())
            .description(dto.description())
            .price(dto.price())
            .menuStatus(MenuStatus.SHOW)
            .shop(shop)
            .build();
    }

    public void updateOf(UpdateMenuRequestDto dto) {
        this.name = dto.name();
        this.description = dto.description();
        this.price = dto.price();
    }

    public void deleteOf(String deletedBy) {
        super.deleteOf(deletedBy);
    }
}

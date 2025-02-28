package com.fourseason.delivery.domain.shop.entity;

import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.*;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.shop.dto.request.CreateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.request.UpdateShopRequestDto;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import com.fourseason.delivery.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_shop")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String tel;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String detailAddress;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public boolean isShopOwner(Long memberId) {
        return getMember().getId().equals(memberId);
    }

    public void checkShopOwner(Long memberId) {
        if (!isShopOwner(memberId)) {
            throw new CustomException(NOT_SHOP_OWNER);
        }
    }

    @Builder
    private Shop(String name, String description, String tel, String address, String detailAddress, Member member, Category category) {
        this.name = name;
        this.description = description;
        this.tel = tel;
        this.address = address;
        this.detailAddress = detailAddress;
        this.member = member;
        this.category = category;
    }

    public static Shop addOf(CreateShopRequestDto dto, Member member, Category category) {
        return Shop.builder()
            .name(dto.name())
            .description(dto.description())
            .tel(dto.tel())
            .address(dto.address())
            .detailAddress(dto.detailAddress())
            .member(member)
            .category(category)
            .build();
    }

    public void updateOf(UpdateShopRequestDto dto, Category category) {
        this.name = dto.name();
        this.description = dto.description();
        this.tel = dto.tel();
        this.address = dto.address();
        this.detailAddress = dto.detailAddress();
        this.category = category;
    }

    public void deleteOf(String deletedBy) {
        super.deleteOf(deletedBy);
    }
}

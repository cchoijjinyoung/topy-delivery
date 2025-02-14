package com.fourseason.delivery.domain.shop.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_shop")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String tel;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String detail_address;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder
    public Shop(String name, String description, String tel, String address, String detail_address, Member member, Category category) {
        this.name = name;
        this.description = description;
        this.tel = tel;
        this.address = address;
        this.detail_address = detail_address;
        this.member = member;
        this.category = category;
    }
}

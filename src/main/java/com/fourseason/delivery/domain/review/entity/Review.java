package com.fourseason.delivery.domain.review.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    private String id;

    private String content;

    private int rating;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Builder
    public Review(String content, int rating, Member member, Order order, Shop shop) {
        this.content = content;
        this.rating = rating;
        this.member = member;
        this.order = order;
        this.shop = shop;
    }
}

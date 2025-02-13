package com.fourseason.delivery.domain.order.entity;

import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_order_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenu extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}

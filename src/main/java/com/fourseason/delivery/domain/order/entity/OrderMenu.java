package com.fourseason.delivery.domain.order.entity;

import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Table(name = "p_order_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenu extends BaseTimeEntity {

  @Id
  @UuidGenerator
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "menu_id")
  private Menu menu;

  @Column(nullable = false)
  private int menuPrice;

  @Column(nullable = false)
  private int quantity;

  public static OrderMenu addOf(Menu menu, int menuPrice, int quantity) {
    return OrderMenu.builder()
        .menu(menu)
        .menuPrice(menuPrice)
        .quantity(quantity)
        .build();
  }

  @Builder
  private OrderMenu(UUID id, int quantity, int menuPrice, Menu menu, Order order) {
    this.id = id;
    this.quantity = quantity;
    this.menuPrice = menuPrice;
    this.menu = menu;
    this.order = order;
  }
}

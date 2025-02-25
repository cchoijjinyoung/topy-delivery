package com.fourseason.delivery.domain.order.entity;

import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.OFFLINE;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ALREADY_CANCELED_ORDER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_ORDERED_BY_CUSTOMER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_OWNER_OR_CUSTOMER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_PENDING_ORDER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_SHOP_OWNER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_CANCEL_EXPIRED;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.LAZY;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import com.fourseason.delivery.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;


@Entity
@Getter
@Table(name = "p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order extends BaseTimeEntity {

  @Id
  @UuidGenerator
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "shop_id", nullable = false)
  private Shop shop;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToMany(fetch = LAZY, cascade = PERSIST)
  private List<OrderMenu> orderMenuList;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus orderStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderType orderType;

  @Column(nullable = false)
  private String address;

  private String instruction;

  @Column(nullable = false)
  private int totalPrice;

  @Builder
  private Order(Shop shop, Member member, List<OrderMenu> orderMenuList,
      OrderStatus orderStatus, OrderType orderType, String address, String instruction,
      int totalPrice) {
    this.shop = shop;
    this.member = member;
    this.orderMenuList = orderMenuList;
    this.orderType = orderType;
    this.orderStatus = orderStatus;
    this.address = address;
    this.instruction = instruction;
    this.totalPrice = totalPrice;
  }

  public static Order addByCustomer(
      Shop shop,
      Member member,
      String address,
      String instruction,
      List<OrderMenu> orderMenuList
  ) {
    return Order.builder()
        .shop(shop)
        .member(member)
        .orderStatus(PENDING)
        .orderType(ONLINE)
        .address(address)
        .instruction(instruction)
        .orderMenuList(orderMenuList)
        .totalPrice(orderMenuList.stream().mapToInt(OrderMenu::getTotalPrice).sum())
        .build();
  }

  public static Order addByOwner(
      Shop shop,
      String address,
      String instruction,
      List<OrderMenu> orderMenuList
  ) {
    return Order.builder()
        .shop(shop)
        .orderStatus(ACCEPTED)
        .orderType(OFFLINE)
        .address(address)
        .instruction(instruction)
        .orderMenuList(orderMenuList)
        .totalPrice(orderMenuList.stream().mapToInt(OrderMenu::getTotalPrice).sum())
        .build();
  }

  public static Order addByManager(
      Shop shop,
      Member customer,
      String address,
      String instruction,
      List<OrderMenu> orderMenuList,
      OrderStatus status,
      OrderType type
  ) {
    return Order.builder()
        .shop(shop)
        .member(customer)
        .address(address)
        .instruction(instruction)
        .orderMenuList(orderMenuList)
        .orderStatus(status)
        .orderType(type)
        .totalPrice(orderMenuList.stream().mapToInt(OrderMenu::getTotalPrice).sum())
        .build();
  }

  public void updateStatus(OrderStatus orderStatus) {
    this.orderStatus = orderStatus;
  }

  public void assertShopOwner(Long ownerId) {
    if (!this.getShop().getMember().getId().equals(ownerId)) {
      throw new CustomException(NOT_SHOP_OWNER);
    }
  }

  public void assertOwnerOrCustomer(Long ownerId) {
    if (!this.getShop().getMember().getId().equals(ownerId)
        && !this.getMember().getId().equals(ownerId)) {
      throw new CustomException(NOT_OWNER_OR_CUSTOMER);
    }
  }

  public void assertOrderIsPending() {
    if (this.orderStatus != OrderStatus.PENDING) {
      throw new CustomException(NOT_PENDING_ORDER);
    }
  }

  public void assertOrderedBy(Long customerId) {
    if (!this.getMember().getId().equals(customerId)) {
      throw new CustomException(NOT_ORDERED_BY_CUSTOMER);
    }
  }

  public void assertExpiredCancelTime(Duration time) {
    if (LocalDateTime.now().isAfter(this.getCreatedAt().plus(time))) {
      throw new CustomException(ORDER_CANCEL_EXPIRED);
    }
  }

  public void assertAlreadyCanceled() {
    if (this.orderStatus == CANCELED) {
      throw new CustomException(ALREADY_CANCELED_ORDER);
    }
  }

  public void assertAlreadyDeleted() {
    if (this.getDeletedAt() != null) {
      throw new CustomException(ALREADY_CANCELED_ORDER);
    }
  }
}

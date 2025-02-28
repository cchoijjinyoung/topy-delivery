package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.MANAGER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.member.entity.Role.valueOf;
import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.MEMBER_UNAUTHORIZED;
import static com.fourseason.delivery.domain.order.constant.OrderConstants.*;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MENU_NOT_FOUND;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_PAID_ORDER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.NOT_SHOP_OWNER;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.ManagerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.SubmitOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.AcceptOfflineOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.OrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.CustomerOrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.ManagerOrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.ManagerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.OwnerOrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OrderSearchRepositoryCustom;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final ShopRepository shopRepository;
  private final MemberRepository memberRepository;
  private final MenuRepository menuRepository;
  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  /**
   * 비대면 주문 요청
   */
  @Transactional
  public UUID createOnlineOrder(SubmitOrderRequestDto request, Long customerId) {
    Shop shop = findShopOrThrow(request.shopId());
    Member loginMember = findMemberOrThrow(customerId);
    List<OrderMenu> orderMenuList = request.menuList().stream()
        .map(menuDto -> {
              Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuDto.id())
                  .orElseThrow(() -> new CustomException(MENU_NOT_FOUND));
              return OrderMenu.addOf(menu, menuDto.quantity());
            }
        ).toList();

    Order createOrder = Order.createByOnlineOrder(
        shop,
        loginMember,
        request.address(),
        request.instruction(),
        orderMenuList
    );
    return orderRepository.save(createOrder).getId();
  }

  /**
   * 대면 주문 접수
   */
  @Transactional
  public UUID createOfflineOrder(AcceptOfflineOrderRequestDto request, Long ownerId) {
    Shop shop = findShopOrThrow(request.shopId());
    if (shop.isShopOwner(ownerId)) {
      throw new CustomException(NOT_SHOP_OWNER);
    }
    List<OrderMenu> orderMenuList = request.menuList().stream()
        .map(menuDto -> {
              Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuDto.id())
                  .orElseThrow(() -> new CustomException(MENU_NOT_FOUND));
              return OrderMenu.addOf(menu, menuDto.quantity());
            }
        ).toList();
    Order createOrder = Order.createByOfflineOrder(
        shop,
        request.address(),
        request.instruction(),
        orderMenuList
    );
    return orderRepository.save(createOrder).getId();
  }

  /**
   * 관리자 권한 주문 생성
   */
  @Transactional
  public UUID createOrder(ManagerCreateOrderRequestDto request) {
    Shop shop = findShopOrThrow(request.shopId());
    Member customer = resolveCustomer(request.customerId());
    List<OrderMenu> orderMenuList = request.menuList().stream()
        .map(menuDto -> {
              Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuDto.id())
                  .orElseThrow(() -> new CustomException(MENU_NOT_FOUND));
              return OrderMenu.addOf(menu, menuDto.quantity());
            }
        ).toList();

    Order createOrder = Order.createByManager(
        shop,
        customer,
        request.address(),
        request.instruction(),
        orderMenuList,
        request.orderStatus(),
        request.orderType());
    return orderRepository.save(createOrder).getId();
  }

  /**
   * 요청 주문 접수
   */
  @Transactional
  public void acceptOrder(UUID orderId, Long ownerId) {
    Order order = findOrderOrThrow(orderId);
    validateOrderAcceptance(ownerId, order);
    Payment payment = findPaymentOrThrow(order);
    if (payment.isPaymentCompleted()) {
      throw new CustomException(NOT_PAID_ORDER);
    }
    order.accepted();
  }

  /**
   * 주문 상세 조회 현재 로그인한 회원의 권한에 따라 검증과 상세데이터가 다릅니다.
   * 고객: 내 주문만 조회 가능
   * 점주: 내 주문과 내 가게에 요청된 주문만 조회 가능
   * 관리자: 모든 주문 조회 가능
   */
  @Transactional(readOnly = true)
  public OrderDetailsResponseDto readOne(UUID orderId, CustomPrincipal principal) {
    Order order = findOrderOrThrow(orderId);

    Role memberRole = valueOf(principal.getRole());
    Long memberId = principal.getId();

    if (memberRole == CUSTOMER) {
      order.checkOrderedBy(memberId);
      return CustomerOrderDetailsResponseDto.of(order);
    }
    if (memberRole == OWNER) {
      order.checkOwnerOrCustomer(memberId);
      return OwnerOrderDetailsResponseDto.of(order);
    }
    if (memberRole == MANAGER) {
      return ManagerOrderDetailsResponseDto.of(order);
    }
    throw new CustomException(MEMBER_UNAUTHORIZED);
  }

  /**
   * 나의 주문 내역 검색
   */
  @Transactional(readOnly = true)
  public PageResponseDto<? extends OrderSummaryResponseDto> searchBy(
      CustomPrincipal principal,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    Role memberRole = valueOf(principal.getRole());
    String username = principal.getName();

    if (memberRole == CUSTOMER) {
      return orderSearchRepositoryCustom.findByCustomerWithPage(username, pageRequestDto, keyword);
    }
    if (memberRole == OWNER) {
      return orderSearchRepositoryCustom.findByOwnerWithPage(username, pageRequestDto, keyword);
    }
    if (memberRole == MANAGER) {
      return orderSearchRepositoryCustom.findByManagerWithPage(username, pageRequestDto, keyword);
    }
    throw new CustomException(MEMBER_UNAUTHORIZED);
  }

  /**
   * 점주의 본인 가게 주문 내역 검색
   */
  @Transactional(readOnly = true)
  public PageResponseDto<OwnerOrderSummaryResponseDto> searchBy(
      Long memberId,
      UUID shopId,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    return orderSearchRepositoryCustom.searchByOwnerWithPage(
        memberId, shopId, pageRequestDto, keyword);
  }

  /**
   * 관리자의 주문 내역 검색
   */
  @Transactional(readOnly = true)
  public PageResponseDto<ManagerOrderSummaryResponseDto> searchBy(
      String customerUsername,
      UUID shopId,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    return orderSearchRepositoryCustom.searchByManagerWithPage(
        customerUsername, shopId, pageRequestDto, keyword);
  }

  /**
   * 주문 취소
   * 권한 별 검증 필요
   */
  @Transactional
  public void cancelOrder(UUID orderId, CustomPrincipal principal) {
    Order order = findOrderOrThrow(orderId);
    Role memberRole = valueOf(principal.getRole());
    Long memberId = principal.getId();
    if (memberRole == CUSTOMER) {
      order.checkOrderedBy(memberId);
    } else if (memberRole == OWNER) {
      order.checkOwnerOrCustomer(memberId);
    }
    order.checkAlreadyCanceled();
    order.checkExpiredCancelTime(ORDER_CANCELED_DEADLINE);
    order.canceled();
  }

  /**
   * 주문 삭제
   */
  @Transactional
  public void deleteOrder(UUID orderId, String username) {
    Order order = findOrderOrThrow(orderId);
    order.checkAlreadyDeleted();
    order.deleteOf(username);
  }


  private void validateOrderAcceptance(Long ownerId, Order order) {
    order.checkOrderIsPending();
    order.getShop().checkShopOwner(ownerId);
  }

  private Member resolveCustomer(Long customerId) {
    if (customerId == null) {
      return null;
    }
    return memberRepository.findByIdAndDeletedAtIsNull(customerId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private Member findMemberOrThrow(Long customerId) {
    return memberRepository.findByIdAndDeletedAtIsNull(customerId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private Shop findShopOrThrow(UUID shopId) {
    return shopRepository.findByIdAndDeletedAtIsNull(shopId)
        .orElseThrow(() -> new CustomException(SHOP_NOT_FOUND));
  }

  private Order findOrderOrThrow(UUID orderId) {
    return orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
  }

  private Payment findPaymentOrThrow(Order order) {
    return paymentRepository.findByOrderAndDeletedAtIsNull(order)
        .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));
  }
}

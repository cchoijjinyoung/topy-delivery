package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.menu.entity.MenuStatus.SHOW;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MENU_NOT_FOUND;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;
import static java.util.stream.Collectors.toMap;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto.MenuDto;
import com.fourseason.delivery.domain.order.dto.response.OrderDetailResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OrderRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCustomerService {

  private final OrderRepository orderRepository;
  private final ShopRepository shopRepository;
  private final MemberRepository memberRepository;
  private final MenuRepository menuRepository;
  private final OrderRepositoryCustom orderRepositoryCustom;

  /**
   * 리팩토링 과제
   */
  @Transactional
  public UUID createOrder(CreateOrderRequestDto request, Long memberId) {

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    Shop shop = shopRepository.findById(request.shopId())
        .orElseThrow(() -> new CustomException(SHOP_NOT_FOUND));

    List<UUID> requestMenuIds = request.menuList().stream().map(MenuDto::menuId).toList();
    List<Menu> menuList = menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
        requestMenuIds, SHOW, shop);

    if (requestMenuIds.size() != menuList.size()) {
      throw new CustomException(MENU_NOT_FOUND);
    }

    Map<UUID, MenuDto> menuDtoMap = request.menuList().stream()
        .collect(toMap(MenuDto::menuId, menuDto -> menuDto));

    int totalPrice = 0;
    List<OrderMenu> orderMenuList = new ArrayList<>();

    for (Menu menu : menuList) {
      OrderMenu orderMenu = OrderMenu.addOf(menu, menuDtoMap.get(menu.getId()).quantity());

      totalPrice += orderMenu.getPrice() * orderMenu.getQuantity();
      orderMenuList.add(orderMenu);
    }

    Order savedOrder = orderRepository.save(
        Order.addByOnlineOrder(request, shop, member, orderMenuList, totalPrice));

    return savedOrder.getId();
  }

  @Transactional(readOnly = true)
  public OrderDetailResponseDto getOrder(UUID orderId, Long memberId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

    order.assertOrderedBy(memberId);

    return OrderDetailResponseDto.of(order);
  }

  @Transactional(readOnly = true)
  public PageResponseDto<OrderSummaryResponseDto> getOrderList(
      Long memberId,
      PageRequestDto pageRequestDto
  ) {
    return orderRepositoryCustom.findOrderListWithPage(memberId, pageRequestDto);
  }
}

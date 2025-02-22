package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.menu.entity.MenuStatus.SHOW;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MENU_NOT_FOUND;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_SHOP_OWNER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;

import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.OwnerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.OwnerCreateOrderRequestDto.MenuDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderDetailResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OwnerOrderRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderOwnerService {

  private final ShopRepository shopRepository;
  private final MenuRepository menuRepository;
  private final OrderRepository orderRepository;
  private final OwnerOrderRepositoryCustom ownerOrderRepositoryCustom;

  @Transactional
  public UUID createOrder(OwnerCreateOrderRequestDto request, Long memberId) {
    Shop shop = shopRepository.findById(request.shopId())
        .orElseThrow(() -> new CustomException(SHOP_NOT_FOUND));

    if (!shop.getMember().getId().equals(memberId)) {
      throw new CustomException(NOT_SHOP_OWNER);
    }

    List<OrderMenu> orderMenuList = createOrderMenuList(request.menuList(), shop);
    int totalPrice = calculateTotalPrice(orderMenuList);

    Order savedOrder = orderRepository.save(
        Order.addByOwner(
            shop,
            request.address(),
            request.instruction(),
            orderMenuList,
            totalPrice));

    return savedOrder.getId();
  }

  @Transactional
  public void acceptOrder(Long ownerId, UUID orderId) {

    Order order = orderRepository.findById(orderId).
        orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

    order.assertShopOwner(ownerId);
    order.assertOrderIsPending();

    order.updateStatus(ACCEPTED);
  }

  @Transactional(readOnly = true)
  public OwnerOrderDetailResponseDto getOrder(UUID orderId, Long memberId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

    order.assertShopOwner(memberId);

    return OwnerOrderDetailResponseDto.of(order);
  }

  @Transactional(readOnly = true)
  public PageResponseDto<OwnerOrderSummaryResponseDto> getOrderList(
      Long memberId,
      UUID shopId,
      PageRequestDto pageRequestDto
  ) {
    return ownerOrderRepositoryCustom.findOrderListWithPage(memberId, shopId, pageRequestDto);
  }

  @Transactional
  public void cancelOrder(UUID orderId, Long memberId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

    order.assertShopOwner(memberId);

    order.updateStatus(CANCELED);
  }

  private List<OrderMenu> createOrderMenuList(List<MenuDto> menuDtoList, Shop shop) {
    List<OrderMenu> result = new ArrayList<>();

    List<UUID> menuIds = menuDtoList.stream().map(MenuDto::menuId).toList();
    List<Menu> menuList = menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
        menuIds, SHOW, shop);

    if (menuIds.size() != menuList.size()) {
      throw new CustomException(MENU_NOT_FOUND);
    }

    Map<UUID, MenuDto> map = menuDtoList.stream()
        .collect(Collectors.toMap(MenuDto::menuId, menuDto -> menuDto));

    for (Menu menu : menuList) {
      MenuDto menuDto = map.get(menu.getId());
      OrderMenu orderMenu = OrderMenu.addOf(menu, menuDto.quantity());
      result.add(orderMenu);
    }
    return result;
  }

  private int calculateTotalPrice(List<OrderMenu> orderMenuList) {
    return orderMenuList.stream().mapToInt(
        orderMenu -> orderMenu.getPrice() * orderMenu.getQuantity()).sum();
  }
}
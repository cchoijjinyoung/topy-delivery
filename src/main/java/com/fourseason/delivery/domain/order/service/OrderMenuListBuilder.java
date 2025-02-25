package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.menu.entity.MenuStatus.SHOW;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MENU_NOT_FOUND;

import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.OrderCreateDto;
import com.fourseason.delivery.domain.order.dto.request.OrderCreateDto.OrderMenuCreateDto;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMenuListBuilder {

  private final MenuRepository menuRepository;

  public List<OrderMenu> create(OrderCreateDto dto) {
    return createOrderMenuList(dto.orderMenuCreateDtoList(), dto.shop());
  }

  private List<OrderMenu> createOrderMenuList(List<OrderMenuCreateDto> dtoList, Shop shop) {
    List<OrderMenu> result = new ArrayList<>();

    List<UUID> menuIds = dtoList.stream().map(OrderMenuCreateDto::menuId).toList();
    List<Menu> menuList = menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
        menuIds, SHOW, shop);

    if (menuIds.size() != menuList.size()) {
      throw new CustomException(MENU_NOT_FOUND);
    }

    Map<UUID, OrderMenuCreateDto> map = dtoList.stream()
        .collect(Collectors.toMap(OrderMenuCreateDto::menuId, menuDto -> menuDto));

    for (Menu menu : menuList) {
      OrderMenuCreateDto dto = map.get(menu.getId());
      OrderMenu orderMenu = OrderMenu.addOf(menu, dto.quantity());
      result.add(orderMenu);
    }
    return result;
  }
}
